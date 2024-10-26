package com.walkingstepcounter.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.walkingstepcounter.R
import com.walkingstepcounter.repository.StepCounterRepository
import com.yourapp.sensor.StepCounterService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterForegroundService : Service() {

    @Inject
    lateinit var stepCounter: StepCounterService

    @Inject
    lateinit var stepCounterRepository: StepCounterRepository

    private val CHANNEL_ID = "StepCounterServiceChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()

        if (stepCounter.isSensorAvailable()) {
            // Combine sensor step counting flow and database flow
            CoroutineScope(Dispatchers.IO).launch {
                stepCounterRepository.startStepCounting()
                    .collect { sensorSteps ->
                        Log.d("aaa", " step counter coroutine step started")
                        Log.d("aaa", " step counter $sensorSteps")

                        // Get the current steps from the database
                        val currentSteps = stepCounterRepository. getCurrentNumOfStep(1).first() ?: 0

                        Log.d("aaa", " currentSteps counter $currentSteps")


                        // Calculate total steps
                        val totalSteps = currentSteps + sensorSteps // Handle nullable currentSteps
                        Log.d(
                            "stepForgroundService",
                            "Sensor Steps: $sensorSteps | Current Steps from DB: $currentSteps | Total Steps: $totalSteps"
                        )

                        // Update the database only if sensor steps have changed
                        if (sensorSteps >= 0) {
                            stepCounterRepository.updateOrInsertCurrentStepCounter(totalSteps)
                            Log.d("aaa", " updated")
                        }
                    }
            }
        } else {
            stopSelf()
        }

        return START_STICKY
    }


                       // Check if the currentSteps is null, use 0 if it is
//                       val totalSteps = currentSteps?.plus(sensorStepCounter)

                       // Log the total steps calculated
//                       Log.d("stepForgroundService", "Total Steps: $totalSteps")

                       // Update or insert the new total steps into the database
//                       if (totalSteps != null) {
//                           stepCounterRepository.updateOrInsertCurrentStepCounter(totalSteps)
//                       }

//                       Log.d("stepForgroundService", "Updated Total Steps in DB: $totalSteps")


        //now when i get the step then i going to check the value in database which is also return int
        /*CoroutineScope(Dispatchers.IO).launch {
                            stepCounterRepository.getCurrentNumOfStep(1).collect{
                                    currentNumberOfDatabase->
                                Log.d("aaaaaaaa", "onStartCommand:  $stepCounter ")
                                //now when i can add the sensor value which is alway 1 and the database value
                                // just assuming that : 1+7
                                //totalSteps = 8
                                val totalSteps = currentNumberOfDatabase + sensorStepCounter

                                //so the 8 is updated in the database
                                CoroutineScope(Dispatchers.IO).launch {
                                    Log.e("aaaaaaaa", "onStartCommand: value updated")
                                    stepCounterRepository.updateOrInsertCurrentStepCounter(totalSteps)
                                }
                            }

                        }*/






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

    override fun onBind(intent: Intent?): IBinder? {
        return null // We are not binding this service
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup code if needed
    }
}
