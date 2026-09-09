package com.sih26168.app.sensors

data class AccelerometerSample(
    val timestampNs: Long,
    val x: Float,
    val y: Float,
    val z: Float
)
