package com.aodstudio.app.aod.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pocket detection manager using Android SensorManager proximity sensor.
 * Automatically detects when device is placed in a pocket/bag and triggers
 * callback to hide overlay view for maximum battery saving.
 */
@Singleton
class PocketSensorManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val proximitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var onPocketStateChangedListener: ((isInPocket: Boolean) -> Unit)? = null
    private var isListening = false
    private var lastStateInPocket = false

    fun startListening(onPocketStateChanged: (isInPocket: Boolean) -> Unit) {
        if (isListening || proximitySensor == null) return
        this.onPocketStateChangedListener = onPocketStateChanged
        sensorManager?.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        isListening = true
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        onPocketStateChangedListener = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return

        val distance = event.values[0]
        val maxRange = proximitySensor?.maximumRange ?: 5f
        val isInPocket = distance < minOf(maxRange, 5f)

        if (isInPocket != lastStateInPocket) {
            lastStateInPocket = isInPocket
            onPocketStateChangedListener?.invoke(isInPocket)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
