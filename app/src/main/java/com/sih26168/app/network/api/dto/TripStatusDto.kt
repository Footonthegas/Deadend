package com.sih26168.app.network.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripStatusDto(
    @SerialName("trip_id")
    val tripId: String,
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String? = null
)
