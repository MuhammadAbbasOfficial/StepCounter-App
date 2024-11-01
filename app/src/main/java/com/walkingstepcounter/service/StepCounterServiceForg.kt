package com.walkingstepcounter.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.walkingstepcounter.R
import com.walkingstepcounter.repository.StepCounterRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterServiceForg : LifecycleService(), SensorEventListener {

    @Inject
    lateinit var stepCounterRepository: StepCounterRepository

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepCountListener: ((Int) -> Unit)? = null
    private var initialStepCount: Int = 0
    private var isInitialCountSet = false
    private var registered = false

    private val CHANNEL_ID = "StepCounterServiceChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForegroundService()

        // Start step counting if sensor is available
        if (isSensorAvailable()) {
            startStepCounting { sensorSteps ->
                Log.d("StepCounterService", "Step Count Updated: $sensorSteps")

                // Combine sensor step counting flow and database flow
                CoroutineScope(Dispatchers.IO).launch {
                    val currentSteps = stepCounterRepository.getCurrentNumOfStep(1).first() ?: 0
                    val totalSteps = currentSteps + sensorSteps

                    Log.d("StepCounterService", "Sensor Steps: $sensorSteps | Current Steps from DB: $currentSteps | Total Steps: $totalSteps")

                    // Update the database only if sensor steps have changed
                    if (sensorSteps > 0) {
                        stepCounterRepository.updateOrInsertCurrentStepCounter(totalSteps)
                        Log.d("StepCounterService", "Steps updated in the database.")
                    }
                }
            }
        } else {
            stopSelf()
        }

        return START_STICKY
    }

    // Method to start step counting
    private fun startStepCounting(listener: (Int) -> Unit) {
        if (isSensorAvailable()) {
            stepCountListener = listener
            stepCounterSensor?.also { sensor ->
                if (!registered) {
                    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
                    registered = true
                    Log.d("StepCounterService", "Sensor registered successfully.")
                }
            }
        } else {
            Log.e("StepCounterService", "Sensor not available.")
            listener.invoke(-1) // Indicate sensor not available
        }
    }

    // Method to stop step counting
    private fun stopStepCounting() {
        if (registered) {
            sensorManager.unregisterListener(this)
            registered = false
            Log.d("StepCounterService", "Sensor unregistered successfully.")
        } else {
            Log.e("StepCounterService", "Sensor was not registered.")
        }
    }

    // SensorEventListener methods
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == stepCounterSensor) {
            val currentStepCount = event?.values?.get(0)?.toInt() ?: 0

            if (!isInitialCountSet) {
                initialStepCount = currentStepCount
                isInitialCountSet = true
            }

            val stepsSinceStart = currentStepCount - initialStepCount
            stepCountListener?.invoke(stepsSinceStart)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    private fun isSensorAvailable(): Boolean {
        return stepCounterSensor != null
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Counter")
            .setContentText("Tracking your steps...")
            .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Step Counter Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        stopStepCounting()
        super.onDestroy()
    }

    override fun onBind(intent: Intent) = super.onBind(intent)
}
