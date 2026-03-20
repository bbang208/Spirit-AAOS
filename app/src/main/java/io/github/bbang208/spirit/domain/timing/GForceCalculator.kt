package io.github.bbang208.spirit.domain.timing

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.bbang208.spirit.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GForceCalculator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var filteredLateral = 0f
    private var filteredLongitudinal = 0f

    fun observeGForce(): Flow<GForceData> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        if (sensor == null) {
            close()
            return@callbackFlow
        }

        filteredLateral = 0f
        filteredLongitudinal = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val alpha = Constants.G_FORCE_FILTER_ALPHA

                val rawLateral = event.values[0] / SensorManager.GRAVITY_EARTH
                val rawLongitudinal = event.values[1] / SensorManager.GRAVITY_EARTH

                // Low-pass filter
                filteredLateral = filteredLateral + alpha * (rawLateral - filteredLateral)
                filteredLongitudinal = filteredLongitudinal + alpha * (rawLongitudinal - filteredLongitudinal)

                trySend(
                    GForceData(
                        lateralG = filteredLateral,
                        longitudinalG = filteredLongitudinal,
                        timestamp = event.timestamp
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
