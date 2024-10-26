package com.yourapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import javax.inject.Inject
class StepCounterService @Inject constructor(
    private val context: Context
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepCountListener: ((Int) -> Unit)? = null
    private var initialStepCount: Int = 0
    private var isInitialCountSet = false

    init {
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    fun isSensorAvailable(): Boolean {
        return stepCounterSensor != null
    }

    fun startStepCounting(listener: (Int) -> Unit) {
        if (isSensorAvailable()) {
            stepCountListener = listener
            stepCounterSensor?.also { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            }
        } else {
            // You can either throw an exception, notify through callback, or handle it internally
            listener.invoke(-1) // Indicate sensor not available with a special value
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == stepCounterSensor) {
            val currentStepCount = event?.values?.get(0)?.toInt() ?: 0

            if (!isInitialCountSet) {
                initialStepCount = currentStepCount
                isInitialCountSet = true
            }

            val stepsSinceStart = currentStepCount - initialStepCount
//            stepCountListener?.invoke(stepsSinceStart)
            stepCountListener?.invoke(1)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    fun stopStepCounting() {
        sensorManager.unregisterListener(this)
    }
    fun resetStepCounting()
    {
        isInitialCountSet = false
    }
}