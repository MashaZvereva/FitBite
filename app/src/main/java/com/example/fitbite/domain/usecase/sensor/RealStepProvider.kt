package com.example.fitbite.domain.usecase.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class RealStepProvider(
    private val context: Context
) : StepProvider, SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var stepsAtStart = -1
    private var currentRawSteps = -1
    private var listener: ((Int) -> Unit)? = null

    override fun start(listener: (steps: Int) -> Unit) {
        this.listener = listener
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            currentRawSteps = event.values[0].toInt()
            if (stepsAtStart == -1) {
                stepsAtStart = currentRawSteps
            }
            val currentSteps = currentRawSteps - stepsAtStart
            listener?.invoke(currentSteps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun getCurrentRawSteps(): Int = currentRawSteps

    fun getStartRawSteps(): Int = stepsAtStart
}
