package com.sih26168.app.navigation

import com.sih26168.app.gnss.GnssSample
import com.sih26168.app.sensors.AccelerometerSample
import com.sih26168.app.sensors.GyroscopeSample

data class NavigationInput(
    val timestampNs: Long,
    val accelerometer: AccelerometerSample,
    val gyroscope: GyroscopeSample,
    val gnss: GnssSample?
)
