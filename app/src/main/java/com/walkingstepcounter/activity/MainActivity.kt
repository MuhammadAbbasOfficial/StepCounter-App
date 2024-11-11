package com.walkingstepcounter.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.walkingstepcounter.viewmodel.StepCounterViewModel
import com.walkingstepcounter.databinding.ActivityMainBinding
import com.walkingstepcounter.service.StepCounterForegroundService
import com.walkingstepcounter.service.StepCounterServiceForg
import com.walkingstepcounter.util.formatElapsedTime
import com.walkingstepcounter.util.getCurrentDate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DialogInterface.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private val stepViewModel: StepCounterViewModel by viewModels()

    // In-App Update variables
    private lateinit var appUpdateManager: AppUpdateManager

    companion object {
        private const val APP_UPDATE_REQUEST_CODE = 100
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }





        stepViewModel.sensorAvailable.observe(this, Observer { it ->
            if (it) {
                stepViewModel.stepCounterRowAvailable.observe(this) { it ->
                    if (it) {
                        stepViewModel.updateCurrentDate(1, getCurrentDate())
                        Log.d("aaa", "onCreate: date updated....")
                    }else{
                        stepViewModel.insertStepCounter(1000, 0)
                    }
                }
            }
        })

        // Initialize AppUpdateManager
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdate()



        binding.setGoal.setOnClickListener{
            showStepGoalDialog()
        }

        binding.age.setOnClickListener{
            showStepAgeDialog()
        }

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        // Handle permission for Android 10 and above
        checkAndRequestPermission()


        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Resume the update.
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    APP_UPDATE_REQUEST_CODE
                )
            }
        }


    }


    private fun updateChart(weeklySteps: List<Pair<String, Int>>) {
        val barEntries = ArrayList<BarEntry>()
//        val barEntries = ArrayList<BarEntry>()
        val daysOfWeek = ArrayList<String>()


        // Mapping of full day names to their short forms
        val dayShortNames = mapOf(
            "Monday" to "Mon",
            "Tuesday" to "Tue",
            "Wednesday" to "Wed",
            "Thursday" to "Thu",
            "Friday" to "Fri",
            "Saturday" to "Sat",
            "Sunday" to "Sun"
        )


        // Calculate total steps
        var totalSteps = 0
        weeklySteps.forEachIndexed { index, pair ->
            val day = pair.first
            val steps = pair.second ?: 0 // Handle null values by defaulting to 0
            totalSteps += steps // Accumulate total steps
            barEntries.add(BarEntry(index.toFloat(), steps.toFloat()))

            // Convert full day name to short form
            val shortDay = dayShortNames[day] ?: day // Use the full name if not found in the map
            daysOfWeek.add(shortDay)
//            daysOfWeek.add(day)
        }

        // Create the data set for the bar chart
        val barDataSet = BarDataSet(barEntries, "Weekly Steps")

        val blueShades = intArrayOf(
            Color.parseColor("#00F1FF"), // Pure Blue
            Color.parseColor("#00F1FF"), // Dodger Blue
            Color.parseColor("#00F1FF"), // Royal Blue
        )

//        barDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        barDataSet.colors = ColorTemplate.createColors(blueShades)
        barDataSet.valueTextSize = 16f
        barDataSet.valueTextColor = Color.WHITE // Set the value text color to white

        // Create the BarData object
        val barData = BarData(barDataSet)
        binding.barChart.data = barData

        // Customize chart appearance
        binding.barChart.description.isEnabled = false
        binding.barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM // Position x-axis at the bottom
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(daysOfWeek)
        binding.barChart.xAxis.textColor = Color.WHITE // Set x-axis text color to white
        binding.barChart.axisLeft.textColor = Color.WHITE // Set left y-axis text color to white
        binding.barChart.axisRight.isEnabled = false // Disable right y-axis
        binding.barChart.axisLeft.setDrawGridLines(false) // Disable grid lines on the left y-axis
        binding.barChart.legend.textColor = Color.WHITE // Set legend text color to white

        // Customize Y-axis
        binding.barChart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return value.toInt().toString() // Convert float to int for display
            }
        }

        // Set Y-axis limits and steps
        stepViewModel.totalNumOfStep.observe(this, Observer {
            binding.barChart.axisLeft.axisMaximum = it.toFloat()
        })
        // Set the maximum value of Y-axis (adjust as necessary)
        binding.barChart.axisLeft.axisMinimum = 0f // Set the minimum value of Y-axis
        binding.barChart.axisLeft.granularity = 100f // Set the granularity to 1000
        binding.barChart.axisLeft.isGranularityEnabled = true // Enable granularity

        // Animate the chart and refresh
        binding.barChart.animateY(1000)
        binding.barChart.invalidate()  // Refresh the chart

        // Display total steps
//        displayTotalSteps(totalSteps)
    }




    private fun showSensorUnavailableMessage() {
        AlertDialog.Builder(this)
            .setTitle("Sensor Unavailable")
            .setMessage("This device does not support step counting.")
            .setPositiveButton("OK", this)
            .show()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                // Handle the dialog positive button click if needed
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopStepCounterService()
        stepViewModel.stopStepCounting()
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Request permission only on Android 10 and above
            if (!isPermissionGranted(android.Manifest.permission.ACTIVITY_RECOGNITION)) {
                requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                // Permission already granted
                accessActivityRecognition()

            }
        } else {
            // For Android 9 and below, no need to request ACTIVITY_RECOGNITION permission
            accessActivityRecognition()

        }
    }

    private fun accessActivityRecognition() {
        // Code to access the sensor or step counting logic here
        Log.d("crashreport", "accessActivityRecognition: -1")

        stepViewModel.sensorAvailable.observe(this, Observer { it ->
            if (it) {
                // stepViewModel.startStepCounting()
                Log.d("crashreport", "accessActivityRecognition: -0")


                stepViewModel.stepCounterRowAvailable.observe(this) { it ->
                    if (it) {
                        startStopTimer()

                        val steps = stepViewModel.currentNumOfStep.value?.toInt()
                        val distance = stepViewModel.distance.value?.toInt()
                        binding.exportBtn.setOnClickListener{
                            val intent = Intent(this, ExportActivty::class.java)
                            val bundle = Bundle()
                            bundle.putInt("steps", steps?:0)
                            bundle.putInt("distance", distance?:0)
                            intent.putExtras(bundle)
                            startActivity(intent)
                            // Apply custom animations for transition
//                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)


//                            // Create ActivityOptions with custom animations
//                            val options = ActivityOptions.makeCustomAnimation(
//                                this,
//                                R.anim.slide_in_right,
//                                R.anim.slide_out_left
//                            )
//
//                            // Start the activity with the specified transition animations
//                            startActivity(intent, options.toBundle())


                        }

                    } else {
                        Log.d("aaa", "accessActivityRecognition: not existed")

                        stepViewModel.insertStepCounter(1000, 0)
                        stepViewModel.loadCurrentNumOfStep(1)
//                        startStepCounterService()
                    }
                }
            } else {
                showSensorUnavailableMessage()
            }
        })

        binding.reset.setOnClickListener {
            binding.circularProgressBar.setWalkingStepCounter("0")
            binding.circularProgressBar.setProgress(0.0)
//            stopStepCounterService()
            stepViewModel.startStepCounting()
            startStepCounterService()
            stepViewModel.resetCurrentNumOfSteps(0)
            resetTimerForNewDay(this)
        }

    }


    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                accessActivityRecognition()

            } else {
                // Permission denied
                Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show()

                if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACTIVITY_RECOGNITION)) {
                    // Show an explanation to the user
                    showPermissionRationale()
                } else {
                    // User denied permission and selected "Don't ask again"
                    showSettingsDialog()
                }
            }
        }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("This app needs the Activity Recognition permission to track your steps. Please allow it for full functionality.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("The Activity Recognition permission is required for step tracking. Please enable it in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }


    private fun showStepGoalDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set Step Goal")
        // Set up the input
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog, _ ->
            val stepGoal = input.text.toString().toIntOrNull()
            if (stepGoal != null) {
                // Pass the value to ViewModel to update total number of steps
                stepViewModel.updateTotalSteps(stepGoal)
                binding.circularProgressBar.setMaxProgress(stepGoal.toFloat())
            } else {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }


    private fun showStepAgeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set Your weight")
        // Set up the input
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog, _ ->
            val age = input.text.toString().toIntOrNull()
            if (age != null) {
                // Pass the value to ViewModel to update total number of steps
                stepViewModel.updateAge(age)
            } else {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }





    private fun startStepCounterService() {
        val serviceIntent = Intent(this, StepCounterForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopStepCounterService() {
        val serviceIntent = Intent(this, StepCounterForegroundService::class.java)
        stopService(serviceIntent)
    }

    private fun checkForAppUpdate() {
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    APP_UPDATE_REQUEST_CODE
                )
            }
        }.addOnFailureListener { exception ->
            // Handle the exception
            Log.e("ErrorUpdateFailed", "checkForAppUpdate: ${exception.message}", )
        }
    }


    private fun startStopTimer()
    {

        binding.startBtn.setOnClickListener{
            binding.startBtn.visibility = View.GONE
            binding.stopBtn.visibility = View.VISIBLE




            stepViewModel.startTimer()



            /*-------------------------------------------------------------------------*/
            Log.d("aaa", "accessActivityRecognition: existed")
            Log.d("crashreport", "accessActivityRecognition: 0")
            startStepCounterService()

            // Checking if the previous date exists; the counter will override
            stepViewModel.handleDateChange()
            // Load the weekly steps data
            stepViewModel.loadWeeklySteps()
            stepViewModel.loadCurrentNumOfStep(1)
            stepViewModel.loadTotalNumOfStep(1)
            stepViewModel.getCalriesBurned()
            stepViewModel.getDistance()

            //calries
            stepViewModel.calries.observe(this, Observer {
                    calries ->

                binding.calries.text = calries.toString()+" Kcal"
            })

            //distance
            stepViewModel.distance.observe(this, Observer {
                    distance ->
                binding.distance.text = distance.toString()+" m"
            })

            //time
            stepViewModel.timerState.observe(this, Observer {
                    timer ->
                Log.d("timer", "accessActivityRecognition: timer is ----------------  $timer")
                binding.time.text = formatElapsedTime(timer.timeSpent)
            })



            stepViewModel.currentNumOfStep.observe(this, Observer {
                it?.toDouble()
                    ?.let { it1 ->
                        Log.d("crashreport", "accessActivityRecognition: 1 = current number of steps : $it1")

                        stepViewModel.totalNumOfStep.observe(this, Observer {
                                it3->
                            Log.d("crashreport", "accessActivityRecognition: 2 and totalnumber of steps = $it3")

                            binding.circularProgressBar.setMaxProgress(it3.toFloat())
                            binding.circularProgressBar.setTotalStepsText(it3.toString())

                            binding.timeProgress.progressMax = it3.toFloat()
                            binding.calriesProgress.progressMax = it3.toFloat()
                            binding.distanceProgress.progressMax = it3.toFloat()


                            if (it1.toInt() >= it3.toInt())
                            {
                                Log.d("crashreport", "accessActivityRecognition: 3")
                                stepViewModel.stopStepCounting()
                                //stopStepCounterService()
                                Log.d("aaa", "accessActivityRecognition: 1= $it1 and 2= $it3 ===== completed")
                                Snackbar.make(
                                    binding.root,
                                    "Reset Your Goal.",
                                    Snackbar.LENGTH_SHORT
                                ).show()

                                binding.circularProgressBar.setProgress(it1)
                                binding.circularProgressBar.setWalkingStepCounter(it3.toString())

                                binding.timeProgress.progress = it1.toFloat()
                                binding.calriesProgress.progress = it1.toFloat()
                                binding.distanceProgress.progress = it1.toFloat()


                            }else
                            {
                                Log.d("crashreport", "accessActivityRecognition: 4")
                                Log.d("aaa", "accessActivityRecognition: 1 = $it1 and 2 = $it3 ===== not completed")

                                binding.circularProgressBar.setWalkingStepCounter(it1.toInt().toString())
                                binding.circularProgressBar.setProgress(it1.toInt().toDouble())


                                binding.timeProgress.progress = it1.toFloat()
                                binding.calriesProgress.progress = it1.toFloat()
                                binding.distanceProgress.progress = it1.toFloat()

                                // Observe the weeklySteps LiveData from the ViewModel
                                stepViewModel.weeklySteps.observe(this, Observer
                                {
                                        weeklySteps ->
                                    updateChart(weeklySteps)
                                })
                            }
                        })
                    }
            })

        }


        binding.stopBtn.setOnClickListener{
            binding.stopBtn.visibility = View.GONE
            binding.startBtn.visibility = View.VISIBLE


            // Switch is OFF, pause the timer
            stepViewModel.stopStepCounting()
            stopStepCounterService()
            stepViewModel.stopTimer()


        }

    }

    private fun resetTimerForNewDay(context: Context) {
        Log.d("timer", "resetTimerForNewDay: run")
        stepViewModel.stopTimer()  // Pause the timer to ensure no ongoing timing
        stepViewModel.resetTimer()  // Reset the timer value in the repository

    }









}
