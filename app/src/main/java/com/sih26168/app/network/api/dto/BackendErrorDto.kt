package com.sih26168.app.network.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackendErrorDto(
    @SerialName("code")
    val code: String? = null,
    @SerialName("message")
    val message: String? = null
)
