package com.sih26168.app.gnss

enum class CompassSpinStage {
    COMPASS_SPIN,
    WAITING_FOR_FIX,
    LEVELING,
    YAW_ALIGNMENT,
    DONE
}
