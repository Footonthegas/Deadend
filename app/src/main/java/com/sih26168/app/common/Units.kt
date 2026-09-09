package com.sih26168.app.common

import kotlin.math.abs

object Units {
    const val MPS_TO_KMH = 3.6

    fun mpsToKmh(mps: Double): Double = mps * MPS_TO_KMH

    /**
     * Normalizes a heading angle to [0, 360).
     */
    fun normalizeHeading(headingDeg: Double): Double {
        var h = headingDeg % 360.0
        if (h < 0) h += 360.0
        return h
    }

    /**
     * Computes the shortest angular difference between two headings.
     * Returns a value in [-180, 180].
     */
    fun headingDiff(heading1: Double, heading2: Double): Double {
        val diff = normalizeHeading(heading1) - normalizeHeading(heading2)
        return when {
            diff > 180.0 -> diff - 360.0
            diff < -180.0 -> diff + 360.0
            else -> diff
        }
    }
}
