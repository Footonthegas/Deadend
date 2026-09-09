package com.sih26168.app.logging

import com.sih26168.app.gnss.GnssStatus
import com.sih26168.app.navigation.NavigationMode

data class LogRecord(
    val timestampNs: Long,
    val accelX: Float, val accelY: Float, val accelZ: Float,
    val gyroX: Float, val gyroY: Float, val gyroZ: Float,
    val gnssLat: Double?, val gnssLon: Double?, val gnssStatus: GnssStatus,
    val navLat: Double?,
    val navLon: Double?,
    val navSpeed: Double,
    val navHeading: Double,
    val navMode: NavigationMode
)
