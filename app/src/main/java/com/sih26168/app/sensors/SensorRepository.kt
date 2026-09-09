package com.sih26168.app.sensors

import com.sih26168.app.common.Time
import com.sih26168.app.gnss.GnssSample
import com.sih26168.app.navigation.NavigationInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SensorRepository {
    private val accelBuffer = SensorBuffer<AccelerometerSample>(100)
    private val gyroBuffer = SensorBuffer<GyroscopeSample>(100)
    private val gnssBuffer = SensorBuffer<GnssSample>(20)

    private val _sensorHealthy = MutableStateFlow(true)
    val sensorHealthy: StateFlow<Boolean> = _sensorHealthy

    fun onAccelerometer(sample: AccelerometerSample) {
        accelBuffer.add(sample)
    }

    fun onGyroscope(sample: GyroscopeSample) {
        gyroBuffer.add(sample)
    }

    fun onGnss(sample: GnssSample) {
        gnssBuffer.add(sample)
    }

    fun createPacket(): NavigationInput? {
        val latestAccel = accelBuffer.getLatest()
        val latestGyro = gyroBuffer.getLatest()

        if (latestAccel == null || latestGyro == null) {
            _sensorHealthy.value = false
            return null
        }

        _sensorHealthy.value = true

        val latestGnss = gnssBuffer.getLatest()

        return NavigationInput(
            timestampNs = Time.monotonicTimestampNs(),
            accelerometer = latestAccel,
            gyroscope = latestGyro,
            gnss = latestGnss
        )
    }
}
