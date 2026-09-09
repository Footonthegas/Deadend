package com.sih26168.app.viewmodel

import com.sih26168.app.gnss.GnssStatus
import com.sih26168.app.gnss.CompassSpinStage
import com.sih26168.app.navigation.NavigationMode

data class NavigationUiState(
    val navigationMode: NavigationMode = NavigationMode.INITIALIZING,
    val gnssStatus: GnssStatus = GnssStatus.WAITING_FOR_FIX,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val speedMps: Double? = null,
    val headingDeg: Double? = null,
    val confidence: Double? = null,
    val roadId: String? = null,
    val mapConfidence: Double? = null,
    val sensorHealthy: Boolean = true,
    val navigationFrequencyHz: Double? = null,
    val errorMessage: String? = null,
    val locationPermissionGranted: Boolean = false,
    val locationServiceEnabled: Boolean = true,

    val compassSpinStage: CompassSpinStage = CompassSpinStage.COMPASS_SPIN,
    val roughHeadingDeg: Double? = null,
    val compassSpinProgress: Float = 0f
)
