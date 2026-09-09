package com.sih26168.app.ai

data class ModelOutput(
    val forwardVelocityMps: Float,
    val velocityVarianceM2s2: Float,
    val valid: Boolean,
    val inferenceLatencyMs: Long
)
