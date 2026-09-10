package com.sih26168.app.network.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripDto(
    @SerialName("trip_id")
    val tripId: String,
    @SerialName("status")
    val status: String? = null,
    @SerialName("created_at")
    val createdAt: Long? = null
)
