package com.sih26168.app.viewmodel

import android.app.Application
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sih26168.app.gnss.CompassManager
import com.sih26168.app.gnss.CompassSpinStage
import com.sih26168.app.gnss.CompassSpinStatus
import com.sih26168.app.gnss.GnssManager
import com.sih26168.app.gnss.GnssStatus
import com.sih26168.app.navigation.NavigationEngineAdapter
import com.sih26168.app.navigation.NavigationMode
import com.sih26168.app.sensors.AccelerometerSource
import com.sih26168.app.sensors.GyroscopeSource
import com.sih26168.app.sensors.SensorRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NavigationViewModel(application: Application) : AndroidViewModel(application) {

    private val accelSource = AccelerometerSource(application)
    private val gyroSource = GyroscopeSource(application)
    private val gnssManager = GnssManager(application)
    private val compassManager = CompassManager(application)
    private val sensorRepo = SensorRepository()
    private val engineAdapter = NavigationEngineAdapter()

    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState: StateFlow<NavigationUiState> = _uiState

    private var navLoopJob: Job? = null
    private var compassSpinJob: Job? = null
    private var gnssJob: Job? = null
    private var lastNavTimeNs = 0L
    private var hzEma = 10.0

    private var spinStartTimeNs = 0L
    private var spinMinHeading = Float.MAX_VALUE
    private var spinMaxHeading = Float.MIN_VALUE

    private var lastKnownLat: Double? = null
    private var lastKnownLon: Double? = null

    init {
        startupChecks()
    }

    private fun startupChecks() {
        val lm = getApplication<Application>().getSystemService(Application.LOCATION_SERVICE) as LocationManager
        val svcEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        _uiState.update { it.copy(locationServiceEnabled = svcEnabled) }
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(locationPermissionGranted = granted) }
        if (granted) {
            viewModelScope.launch {
                val lastLocation = gnssManager.getLastLocation()
                if (lastLocation != null && lastLocation.latitude != null && lastLocation.longitude != null) {
                    _uiState.update {
                        it.copy(
                            latitude = lastLocation.latitude,
                            longitude = lastLocation.longitude
                        )
                    }
                    lastKnownLat = lastLocation.latitude
                    lastKnownLon = lastLocation.longitude
                }
            }
        }
    }

    fun startCompassSpin() {
        if (compassSpinJob?.isActive == true) return

        spinStartTimeNs = 0
        spinMinHeading = Float.MAX_VALUE
        spinMaxHeading = Float.MIN_VALUE

        _uiState.update {
            it.copy(
                compassSpinStage = CompassSpinStage.COMPASS_SPIN,
                roughHeadingDeg = null,
                compassSpinProgress = 0f
            )
        }

        compassSpinJob = viewModelScope.launch {
            compassManager.getFlow().collect { sample ->
                _uiState.update { it.copy(roughHeadingDeg = sample.headingDeg.toDouble()) }

                if (spinStartTimeNs == 0L) {
                    spinStartTimeNs = sample.timestampNs
                }

                spinMinHeading = minOf(spinMinHeading, sample.headingDeg)
                spinMaxHeading = maxOf(spinMaxHeading, sample.headingDeg)

                val coveredRange = spinMaxHeading - spinMinHeading
                val timeElapsedS = (sample.timestampNs - spinStartTimeNs) / 1e9

                val progress = (coveredRange / 300f).coerceIn(0f, 1f)

                _uiState.update { it.copy(compassSpinProgress = progress) }

                val accuracyLevel = compassManager.status.value
                val accuracyHigh = accuracyLevel == CompassSpinStatus.AccuracyHigh
                val rangeCovered = coveredRange >= 300f
                val timeoutReached = timeElapsedS >= 8.0

                if (rangeCovered || (accuracyHigh && timeElapsedS >= 2.0) || timeoutReached) {
                    compassSpinJob?.cancel()
                    completeCompassSpin()
                }
            }
        }
    }

    private fun completeCompassSpin() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    compassSpinStage = CompassSpinStage.WAITING_FOR_FIX,
                    roughHeadingDeg = null,
                    compassSpinProgress = 0f
                )
            }

            delay(500)
            _uiState.update { it.copy(compassSpinStage = CompassSpinStage.LEVELING) }
            delay(500)
            _uiState.update { it.copy(compassSpinStage = CompassSpinStage.YAW_ALIGNMENT) }
            delay(500)

            _uiState.update {
                it.copy(
                    compassSpinStage = CompassSpinStage.DONE,
                    roughHeadingDeg = null
                )
            }
            startNavigation()
        }
    }

    fun skipCompassSpin() {
        compassSpinJob?.cancel()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    compassSpinStage = CompassSpinStage.DONE,
                    roughHeadingDeg = null,
                    compassSpinProgress = 0f
                )
            }
            startNavigation()
        }
    }

    fun startNavigation() {
        if (navLoopJob?.isActive == true) return

        viewModelScope.launch {
            accelSource.getFlow().collect { sensorRepo.onAccelerometer(it) }
        }
        viewModelScope.launch {
            gyroSource.getFlow().collect { sensorRepo.onGyroscope(it) }
        }
        gnssJob = viewModelScope.launch {
            gnssManager.getFlow().collect { sensorRepo.onGnss(it) }
        }
        viewModelScope.launch {
            gnssManager.status.collect { status ->
                _uiState.update { it.copy(gnssStatus = status) }
            }
        }
        viewModelScope.launch {
            sensorRepo.sensorHealthy.collect { healthy ->
                _uiState.update { it.copy(sensorHealthy = healthy) }
            }
        }

        navLoopJob = viewModelScope.launch {
            while (true) {
                processNavigationFrame()
                delay(100)
            }
        }
    }

    fun stopNavigation() {
        navLoopJob?.cancel()
        navLoopJob = null
        gnssJob?.cancel()
        gnssJob = null
        compassSpinJob?.cancel()
        compassSpinJob = null
    }

    private suspend fun processNavigationFrame() {
        val input = sensorRepo.createPacket() ?: return

        if (lastNavTimeNs > 0) {
            val dtS = (input.timestampNs - lastNavTimeNs) / 1e9
            if (dtS > 0) {
                val hz = 1.0 / dtS
                hzEma = hzEma * 0.9 + hz * 0.1
            }
        }
        lastNavTimeNs = input.timestampNs

        val output = engineAdapter.processInput(input)

        if (output.latitude != null) lastKnownLat = output.latitude
        if (output.longitude != null) lastKnownLon = output.longitude

        _uiState.update {
            it.copy(
                navigationMode = output.mode,
                latitude = output.latitude ?: lastKnownLat,
                longitude = output.longitude ?: lastKnownLon,
                speedMps = output.speedMps,
                headingDeg = output.headingDeg,
                confidence = output.confidence,
                roadId = output.debug.roadId,
                mapConfidence = output.debug.mapConfidence,
                navigationFrequencyHz = hzEma
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopNavigation()
    }
}
