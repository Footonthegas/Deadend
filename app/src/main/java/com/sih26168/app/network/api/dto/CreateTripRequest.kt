package com.sih26168.app.network.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTripRequest(
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("auth_token")
    val authToken: String? = null
)
