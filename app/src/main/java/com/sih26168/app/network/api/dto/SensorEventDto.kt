package com.sih26168.app.network.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorEventDto(
    val timestampNs: Long = 0L,

    @SerialName("accel_x")
    val accelX: Float? = null,
    @SerialName("accel_y")
    val accelY: Float? = null,
    @SerialName("accel_z")
    val accelZ: Float? = null,

    @SerialName("gyro_x")
    val gyroX: Float? = null,
    @SerialName("gyro_y")
    val gyroY: Float? = null,
    @SerialName("gyro_z")
    val gyroZ: Float? = null,

    @SerialName("gnss_lat")
    val gnssLat: Double? = null,
    @SerialName("gnss_lon")
    val gnssLon: Double? = null,
    @SerialName("gnss_altitude_m")
    val gnssAltitudeM: Double? = null,
    @SerialName("gnss_speed_mps")
    val gnssSpeedMps: Double? = null,
    @SerialName("gnss_heading_deg")
    val gnssHeadingDeg: Double? = null,
    @SerialName("gnss_accuracy_m")
    val gnssHorizontalAccuracyM: Float? = null,
    @SerialName("gnss_valid")
    val gnssValid: Boolean? = null,

    @SerialName("nav_lat")
    val navLat: Double? = null,
    @SerialName("nav_lon")
    val navLon: Double? = null,
    @SerialName("nav_altitude_m")
    val navAltitudeM: Double? = null,
    @SerialName("nav_speed_mps")
    val navSpeedMps: Double? = null,
    @SerialName("nav_heading_deg")
    val navHeadingDeg: Double? = null,
    @SerialName("nav_confidence")
    val navConfidence: Double? = null,
    @SerialName("nav_mode")
    val navMode: String? = null
)
