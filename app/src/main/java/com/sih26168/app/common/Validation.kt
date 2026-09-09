package com.sih26168.app.common

object Validation {
    /**
     * Checks if a float array contains only finite values.
     */
    fun isFinite(values: FloatArray): Boolean {
        for (v in values) {
            if (v.isNaN() || v.isInfinite()) return false
        }
        return true
    }

    /**
     * Checks if a float contains a finite value.
     */
    fun isFinite(value: Float): Boolean {
        return !value.isNaN() && !value.isInfinite()
    }
    
    /**
     * Checks if a double contains a finite value.
     */
    fun isFinite(value: Double): Boolean {
        return !value.isNaN() && !value.isInfinite()
    }

    /**
     * Verifies that the new timestamp is strictly greater than or equal to the previous.
     */
    fun isMonotonic(prevNs: Long, currentNs: Long): Boolean {
        return currentNs >= prevNs
    }
}
