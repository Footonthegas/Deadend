package com.sih26168.app.gnss

enum class GnssStatus {
    PERMISSION_DENIED,
    PROVIDER_DISABLED,
    WAITING_FOR_FIX,
    AVAILABLE,
    DEGRADED,
    REJECTED,
    OUTAGE
}
