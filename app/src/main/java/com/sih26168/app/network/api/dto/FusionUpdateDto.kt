package com.sih26168.app.network.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FusionUpdateDto(
    @SerialName("timestamp_ns")
    val timestampNs: Long? = null,
    @SerialName("latitude")
    val latitude: Double? = null,
    @SerialName("longitude")
    val longitude: Double? = null,
    @SerialName("altitude_m")
    val altitudeM: Double? = null,
    @SerialName("heading_deg")
    val headingDeg: Double? = null,
    @SerialName("speed_mps")
    val speedMps: Double? = null,
    @SerialName("confidence")
    val confidence: Double? = null,
    @SerialName("mode")
    val mode: String? = null
)
