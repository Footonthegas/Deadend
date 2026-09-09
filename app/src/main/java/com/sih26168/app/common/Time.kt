package com.sih26168.app.common

import android.os.SystemClock

object Time {
    /**
     * Returns a monotonic high-resolution timestamp in nanoseconds.
     */
    fun monotonicTimestampNs(): Long {
        return SystemClock.elapsedRealtimeNanos()
    }
    
    /**
     * Helper to convert nanoseconds to milliseconds.
     */
    fun nsToMs(ns: Long): Long {
        return ns / 1_000_000L
    }
}
