package com.sih26168.app.navigation

data class NavigationOutput(
    val timestampNs: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitudeM: Double,
    val speedMps: Double,
    val headingDeg: Double,
    val mode: NavigationMode,
    val confidence: Double,
    val ekfPositionEnuM: Map<String, Double> = mapOf("east" to 0.0, "north" to 0.0, "up" to 0.0),
    val mapPositionEnuM: Map<String, Double> = mapOf("east" to 0.0, "north" to 0.0, "up" to 0.0),
    val debug: NavigationDebug
)

data class NavigationDebug(
    val gnssValid: Boolean,
    val roadId: String?,
    val mapConfidence: Double
)
