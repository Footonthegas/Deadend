package com.sih26168.app.sensors

data class GyroscopeSample(
    val timestampNs: Long,
    val x: Float,
    val y: Float,
    val z: Float
)
