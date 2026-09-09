package com.sih26168.app.gnss

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.sih26168.app.common.Time
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

class CompassManager(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magneticField: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _status: MutableStateFlow<CompassSpinStatus> = MutableStateFlow(CompassSpinStatus.Idle)
    val status: StateFlow<CompassSpinStatus> = _status

    /**
     * Collects raw magnetic field sensor readings and computes a rough heading.
     * This is CLIENT-ONLY - the heading is never sent to the EKF or backend.
     */
    fun getFlow(): Flow<CompassSample> = callbackFlow {
        if (magneticField == null) {
            _status.value = CompassSpinStatus.SensorUnavailable
            close(IllegalStateException("Magnetic field sensor not available"))
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    val sample = CompassSample(
                        timestampNs = Time.monotonicTimestampNs(),
                        headingDeg = normalizeHeading(computeHeading(event.values[0], event.values[1], event.values[2])),
                        accuracy = event.accuracy
                    )
                    trySend(sample)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                _status.value = when (accuracy) {
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> CompassSpinStatus.AccuracyLow
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> CompassSpinStatus.AccuracyLow
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassSpinStatus.AccuracyMedium
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassSpinStatus.AccuracyHigh
                    else -> CompassSpinStatus.AccuracyLow
                }
            }
        }

        sensorManager.registerListener(listener, magneticField, SensorManager.SENSOR_DELAY_GAME)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    private fun computeHeading(x: Float, y: Float, z: Float): Float {
        val headingRad = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble())).toFloat()
        return if (headingRad < 0) headingRad + 360f else headingRad
    }

    private fun normalizeHeading(heading: Float): Float {
        var h = heading
        while (h < 0) h += 360f
        while (h >= 360) h -= 360f
        return h
    }
}

data class CompassSample(
    val timestampNs: Long,
    val headingDeg: Float,
    val accuracy: Int
)

sealed interface CompassSpinStatus {
    object Idle : CompassSpinStatus
    object AccuracyLow : CompassSpinStatus
    object AccuracyMedium : CompassSpinStatus
    object AccuracyHigh : CompassSpinStatus
    object SensorUnavailable : CompassSpinStatus
}
