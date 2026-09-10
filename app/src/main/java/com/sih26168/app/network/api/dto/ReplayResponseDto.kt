package com.sih26168.app.network.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReplayResponseDto(
    @SerialName("trip_id")
    val tripId: String,
    @SerialName("events")
    val events: List<SensorEventDto> = emptyList()
)
