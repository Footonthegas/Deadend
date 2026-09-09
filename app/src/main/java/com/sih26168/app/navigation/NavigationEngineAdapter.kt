package com.sih26168.app.navigation

class NavigationEngineAdapter : NavigationClient {
    private var currentLat: Double? = null
    private var currentLon: Double? = null
    private var currentHeading = 0.0
    private var currentSpeed = 0.0
    private var initialized = false
    private var initStartTime = 0L
    private var lastTimestampNs = 0L

    override fun processInput(input: NavigationInput): NavigationOutput {
        val dtS = if (lastTimestampNs > 0L) {
            (input.timestampNs - lastTimestampNs) / 1e9
        } else 0.0
        lastTimestampNs = input.timestampNs

        if (!initialized) {
            if (initStartTime == 0L) initStartTime = input.timestampNs
            if (input.timestampNs - initStartTime > 5_000_000_000L) {
                initialized = true
            }

            return NavigationOutput(
                timestampNs = input.timestampNs,
                latitude = currentLat,
                longitude = currentLon,
                altitudeM = 0.0,
                speedMps = 0.0,
                headingDeg = 0.0,
                mode = NavigationMode.INITIALIZING,
                confidence = 0.0,
                debug = NavigationDebug(
                    gnssValid = input.gnss?.valid == true,
                    roadId = null,
                    mapConfidence = 0.0
                )
            )
        }

        val isGnssValid = input.gnss?.valid == true && input.gnss.latitude != null && input.gnss.longitude != null

        val mode = if (isGnssValid) NavigationMode.GNSS_FUSED else NavigationMode.DEAD_RECKONING
        var confidence = if (isGnssValid) 0.9 else 0.6

        if (isGnssValid) {
            val newLat = input.gnss.latitude
            val newLon = input.gnss.longitude

            if (newLat != null && newLon != null) {
                currentLat = newLat
                currentLon = newLon
            }
            if (input.gnss.speedMps != null && input.gnss.speedMps > 0) {
                currentSpeed = input.gnss.speedMps
            }
            if (input.gnss.headingDeg != null) {
                currentHeading = input.gnss.headingDeg
            }
            confidence = 0.95
        } else {
            currentHeading += (input.gyroscope.z * -1.0) * dtS
            currentHeading = normalizeHeading(currentHeading)
            confidence = 0.6
        }

        return NavigationOutput(
            timestampNs = input.timestampNs,
            latitude = currentLat,
            longitude = currentLon,
            altitudeM = input.gnss?.altitudeM ?: 0.0,
            speedMps = currentSpeed,
            headingDeg = currentHeading,
            mode = mode,
            confidence = confidence,
            debug = NavigationDebug(
                gnssValid = isGnssValid,
                roadId = null,
                mapConfidence = if (isGnssValid) 0.8 else 0.3
            )
        )
    }

    private fun normalizeHeading(heading: Double): Double {
        var h = heading % 360.0
        if (h < 0) h += 360.0
        return h
    }
}
