package com.yourapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
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
    private var registered = false

    private val callerTrackingMap = mutableMapOf<String, Int>()



    init {
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    fun isSensorAvailable(): Boolean {
        return stepCounterSensor != null
    }

    fun startStepCounting(listener: (Int) -> Unit) {

        registered = true



        if (registered)
        {
            if (isSensorAvailable()) {
                stepCountListener = listener
                stepCounterSensor?.also { sensor ->
                    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
                    Log.d("StepCounterService", "onSensorChanged: sensor registered.......$registered")

                }

            } else {
                // You can either throw an exception, notify through callback, or handle it internally
                listener.invoke(-1) // Indicate sensor not available with a special value
                registered = false
            }
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
            if (registered)
            {
                stepCountListener?.invoke(1)



                Log.d("StepCounterService", "onSensorChanged: sensor registered.......$registered")


                // Identify the caller class
                val callerClassName = Throwable().stackTrace[1].className

                // Track the number of calls per class
                callerTrackingMap[callerClassName] = (callerTrackingMap[callerClassName] ?: 0) + 1

                // Log or print the call count
                Log.d("StepCounterService", "Called by:   $callerClassName ,   Call count:  ${callerTrackingMap[callerClassName]}")




            }else{
                Log.d("StepCounterService", "onSensorChanged: sensor unregistered.......$registered")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    fun stopStepCounting() {
        if (sensorManager != null) {
            registered = false
            sensorManager.unregisterListener(this)
            Log.d("StepCounterService", "Sensor unregistered successfully.")
        } else {
            registered = true
            Log.e("StepCounterService", "Failed to unregister: sensorManager is null.")
        }
    }
    fun resetStepCounting()
    {
        isInitialCountSet = false
    }
}
