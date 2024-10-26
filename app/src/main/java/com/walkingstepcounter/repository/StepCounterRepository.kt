package com.walkingstepcounter.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.walkingstepcounter.room.StepCounterDAO
import com.walkingstepcounter.room.StepCounterEntity
import com.walkingstepcounter.room.TimerState
import com.walkingstepcounter.util.getCurrentDate
import com.walkingstepcounter.util.getCurrentDayOfWeek
import com.yourapp.sensor.StepCounterService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class StepCounterRepository @Inject constructor(private val dao : StepCounterDAO, private val stepCounterService: StepCounterService) {


    suspend fun insertStepCounter(totalNumStep : Int, currentNumStep : Int, currentDate : String, currentDay : String)
    {
        dao.insertTotalNumOfStep(StepCounterEntity(
            1, getAgeOrDefault(1), totalNumStep, currentNumStep,
            currentDate,"aaa", currentDay,
            monday = if (currentDay == "Monday") currentNumStep else 0,
            tuesday = if (currentDay== "Tuesday") currentNumStep else 0,
            wednesday = if (currentDay == "Wednesday") currentNumStep else 0,
            thursday = if (currentDay == "Thursday") currentNumStep else 0,
            friday = if (currentDay == "Friday") currentNumStep else 0,
            saturday = if (currentDay == "Saturday") currentNumStep else 0,
            sunday = if (currentDay == "Sunday") currentNumStep else 0
        ))
    }

    fun getStepCounterById(id : Int) : LiveData<StepCounterEntity?>
    {
        return dao.selectStepCounterById(id)
    }

    fun getAllStepCounters() : LiveData<List<StepCounterEntity>>
    {
        return dao.getAllStepCounters()
    }

    suspend fun updateStepCounter(stepCounterEntity: StepCounterEntity)
    {
        dao.update(stepCounterEntity)
    }

    suspend fun deleteStepCounterEntity(stepCounterEntity: StepCounterEntity)
    {
        dao.delete(stepCounterEntity)
    }

    suspend fun updateOrInsertCurrentStepCounter(currentNumOfSteps : Int)
    {
        val stepCounter = dao.selectStepCounterById(1)
        if (stepCounter != null)
        {
            if (!isGoalReached())
            {
                Log.d("test", "updateOrInsertCurrentStepCounter: if :   $currentNumOfSteps")
                dao.updateCurrentStep(1, currentNumOfSteps)
                when(getCurrentDayOfWeek())
                {
                    "Monday" -> dao.updateMondaySteps(1, currentNumOfSteps)
                    "Tuesday" -> dao.updateTuesdaySteps(1, currentNumOfSteps)
                    "Wednesday" -> dao.updateWednesdaySteps(1, currentNumOfSteps)
                    "Thursday" -> dao.updateThursdaySteps(1, currentNumOfSteps)
                    "Friday" -> dao.updateFridaySteps(1, currentNumOfSteps)
                    "Saturday" -> dao.updateSaturdaySteps(1, currentNumOfSteps)
                    "Sunday" -> dao.updateSundaySteps(1, currentNumOfSteps)
                }
            }
        }else{
            Log.d("test", "updateOrInsertCurrentStepCounter: else:  $currentNumOfSteps")

            dao.insertTotalNumOfStep(
                StepCounterEntity(
                    1, getAgeOrDefault(1), 1000, 0,
                    getCurrentDate(), "aaa", getCurrentDayOfWeek(),
                    monday = if (getCurrentDayOfWeek() == "Monday") currentNumOfSteps else 0,
                    tuesday = if (getCurrentDayOfWeek() == "Tuesday") currentNumOfSteps else 0,
                    wednesday = if (getCurrentDayOfWeek() == "Wednesday") currentNumOfSteps else 0,
                    thursday = if (getCurrentDayOfWeek() == "Thursday") currentNumOfSteps else 0,
                    friday = if (getCurrentDayOfWeek() == "Friday") currentNumOfSteps else 0,
                    saturday = if (getCurrentDayOfWeek() == "Saturday") currentNumOfSteps else 0,
                    sunday = if (getCurrentDayOfWeek() == "Sunday") currentNumOfSteps else 0
                )
            )
        }
    }

    suspend fun isGoalReached(): Boolean {
        // Fetch currentNumOfStep and totalNumOfSteps from the database for the entity with id = 1
        val currentNumOfSteps = dao.getCurrentNumOfStepById(1).firstOrNull()
        val totalNumOfSteps = dao.getTotalNumOfStepById(1).firstOrNull()

        // Check if the currentNumOfStep equals totalNumOfSteps
        return if (currentNumOfSteps != null && totalNumOfSteps != null) {
            currentNumOfSteps >= totalNumOfSteps
        } else {
            false // Return false if either value is null (meaning no data is present)
        }
    }


    suspend fun resetCurrentNumStepCounter(currentNumOfSteps : Int)
    {
        val stepCounter = dao.selectStepCounterById(1)
        if (stepCounter != null)
        {
            Log.d("test", "updateOrInsertCurrentStepCounter: if :   $currentNumOfSteps")
            dao.updateCurrentStep(1, currentNumOfSteps)
            when(getCurrentDayOfWeek())
            {
                "Monday" -> dao.updateMondaySteps(1, currentNumOfSteps)
                "Tuesday" -> dao.updateTuesdaySteps(1, currentNumOfSteps)
                "Wednesday" -> dao.updateWednesdaySteps(1, currentNumOfSteps)
                "Thursday" -> dao.updateThursdaySteps(1, currentNumOfSteps)
                "Friday" -> dao.updateFridaySteps(1, currentNumOfSteps)
                "Saturday" -> dao.updateSaturdaySteps(1, currentNumOfSteps)
                "Sunday" -> dao.updateSundaySteps(1, currentNumOfSteps)
            }
        }else{
            Log.d("test", "updateOrInsertCurrentStepCounter: else:  $currentNumOfSteps")

            dao.insertTotalNumOfStep(
                StepCounterEntity(
                    1, getAgeOrDefault(1),1000, 0,
                    getCurrentDate(), "aaa", getCurrentDayOfWeek(),
                    monday = if (getCurrentDayOfWeek() == "Monday") currentNumOfSteps else 0,
                    tuesday = if (getCurrentDayOfWeek() == "Tuesday") currentNumOfSteps else 0,
                    wednesday = if (getCurrentDayOfWeek() == "Wednesday") currentNumOfSteps else 0,
                    thursday = if (getCurrentDayOfWeek() == "Thursday") currentNumOfSteps else 0,
                    friday = if (getCurrentDayOfWeek() == "Friday") currentNumOfSteps else 0,
                    saturday = if (getCurrentDayOfWeek() == "Saturday") currentNumOfSteps else 0,
                    sunday = if (getCurrentDayOfWeek() == "Sunday") currentNumOfSteps else 0
                )
            )
        }
    }


    fun getCurrentNumOfStep(id: Int): Flow<Int?> {
        return dao.getCurrentNumOfStepById(id).onStart {
            Log.d("test", "Fetching currentNumOfStep for ID: $id")
        }.onEach { stepCount ->
            Log.d("test", "Fetched currentNumOfStep: $stepCount")

        }
    }
    fun getTotalNumOfStep(id: Int): Flow<Int?> {
        return dao.getTotalNumOfStepById(id).onStart {
            Log.d("test", "Fetching totalNumOfStep for ID: $id")
        }.onEach { stepCount ->
            Log.d("test", "Fetched totalNumOfStep: $stepCount")
        }
    }


    suspend fun isRowExisted(id: Int): Boolean {
        return dao.isRowExisted(id)
    }


    suspend fun updateTotalSteps(totalNumOfSteps: Int) {
        val stepCounter = dao.selectStepCounterById(1)
        if (stepCounter != null) {
            dao.updateTotalNumOfSteps(1, totalNumOfSteps)
        } else {
            // Optionally, handle the case where the entity doesn't exist
            dao.insertTotalNumOfStep(StepCounterEntity(
                id = 1,
                age = getAgeOrDefault(1),
                totalNumOfSteps = totalNumOfSteps,
                currentNumOfStep = 0,
                currentDate = getCurrentDate(),
                "aaa",
                currentDay = getCurrentDayOfWeek(),
                monday = 0, tuesday = 0, wednesday = 0, thursday = 0, friday = 0, saturday = 0, sunday = 0
            ))
        }
    }


    suspend fun getStoredCurrentDate(): String? {
        return dao.getCurrentDateById(1) // Assuming id = 1 for your single row
    }



    fun startStepCounting(): Flow<Int> {
        return callbackFlow {
            // Start counting steps and receive counts through the callback
            stepCounterService.startStepCounting { count ->
                Log.d("test", "startStepCounting: sensor : $count")
                // Send the count value to the flow
                trySend(count)
            }
            // Wait for close to stop listening for sensor data
            awaitClose {
                // Clean up if needed (e.g., stop the sensor)
                stepCounterService.stopStepCounting()
            }
        }
    }



    suspend fun getWeeklySteps(): List<Pair<String, Int>> {
        val weeklyData: List<Pair<String, Int?>> = listOf(
            "Monday" to dao.getMondaySteps(1),
            "Tuesday" to dao.getTuesdaySteps(1),
            "Wednesday" to dao.getWednesdaySteps(1),
            "Thursday" to dao.getThursdaySteps(1),
            "Friday" to dao.getFridaySteps(1),
            "Saturday" to dao.getSaturdaySteps(1),
            "Sunday" to dao.getSundaySteps(1)
        )
        // Map nullable Int? to non-nullable Int, providing a default value (e.g., 0) if null
        return weeklyData.map { pair ->
            pair.first to (pair.second ?: 0)
        }
    }




    // Function to update the current date
    suspend fun updateCurrentDate(id: Int, currentDate: String) {
        dao.updateCurrentDate(id, currentDate)
    }


    suspend fun getAgeOrDefault(id: Int): Int {
        val age = dao.getAgeById(id)
        return age ?: 80 // Return 80 if age is null
    }

    suspend fun updateAge(id: Int, age: Int) {
        dao.updateAge(id, age)
    }




    // Function to calculate calories burned using Flow and return result with 2 decimals
    fun getCaloriesBurned(): Flow<Double> {
        // Fetch the current number of steps from the database using Flow
        val currentNumOfStepsFlow = dao.getCurrentNumOfStepById(1) // Flow of steps

        // Map the step count to the calorie burn calculation
        return currentNumOfStepsFlow.map { currentNumOfSteps ->
            val steps = currentNumOfSteps ?: 0  // Handle null case if no steps exist

            val userWeightKg = getAgeOrDefault(1)  // Fetch user weight
            // Conversion factor for calories burned per step based on weight
            val caloriesPerStep = 0.04 + ((userWeightKg - 70) * 0.001)

            // Calculate total calories burned
            val caloriesBurned = steps * caloriesPerStep

            // Round the calories burned to 2 decimal places
            val roundedCaloriesBurned = BigDecimal(caloriesBurned).setScale(2, RoundingMode.HALF_EVEN).toDouble()

            // Log the calculation
            Log.d("kcalTest", "currentNumOfSteps: $steps")
            Log.d("kcalTest", "caloriesPerStep: $caloriesPerStep")
            Log.d("kcalTest", "totalCaloriesBurned (rounded): $roundedCaloriesBurned")

            // Return the rounded calories burned
            roundedCaloriesBurned
        }
    }

    // Function to calculate distance in real-time using Flow and return result with 2 decimals
    fun calculateDistance(): Flow<Double> {
        // Fetch the current number of steps from the database using Flow
        val currentNumOfStepsFlow = dao.getCurrentNumOfStepById(1) // Flow of steps

        // Map the step count to the distance calculation
        return currentNumOfStepsFlow.map { currentNumOfSteps ->
            val steps = currentNumOfSteps ?: 0  // Handle null case if no steps exist

            // Default user height (or allow user input for height)
            val userHeight = 1.7  // Default height is 1.7 meters

            // Estimate step length based on height
            val stepLength = userHeight * 0.415f  // in meters

            // Calculate the total distance covered
            val distanceCovered = steps * stepLength  // in meters

            // Round the distance to 2 decimal places
            val roundedDistanceCovered = BigDecimal(distanceCovered).setScale(1, RoundingMode.HALF_EVEN).toDouble()

            // Log the calculation
            Log.d("distanceTest", "currentNumOfSteps: $steps")
            Log.d("distanceTest", "stepLength: $stepLength meters")
            Log.d("distanceTest", "totalDistanceCovered (rounded): $roundedDistanceCovered meters")

            // Return the rounded distance covered
            roundedDistanceCovered
        }
    }


    suspend fun updateTimerState(id: Int, timeSpent: Long, timerState: String) {
        dao.updateTimerState11(id, timeSpent, timerState)
    }

    suspend fun resetTimer(id: Int, timeSpent: Long, timerState: String) {
        dao.resetTimer(id, timeSpent, timerState)
    }

    fun getTimerStateById(id: Int): LiveData<TimerState> {
        return dao.getTimerStateById(id)
    }







}