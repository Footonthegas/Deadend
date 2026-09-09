package com.sih26168.app.gnss

data class GnssSample(
    val timestampNs: Long,
    val valid: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val altitudeM: Double?,
    val speedMps: Double?,
    val headingDeg: Double?,
    val horizontalAccuracyM: Float?
)
