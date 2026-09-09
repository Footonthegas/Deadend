package com.sih26168.app.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.sih26168.app.common.Validation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GyroscopeSource(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    fun getFlow(): Flow<GyroscopeSample> = callbackFlow {
        if (gyroscope == null) {
            close(IllegalStateException("Gyroscope not available"))
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    val sample = GyroscopeSample(
                        timestampNs = event.timestamp,
                        x = event.values[0],
                        y = event.values[1],
                        z = event.values[2]
                    )
                    
                    if (Validation.isFinite(sample.x) && Validation.isFinite(sample.y) && Validation.isFinite(sample.z)) {
                        trySend(sample)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
