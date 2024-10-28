package com.walkingstepcounter.viewmodel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingstepcounter.repository.StepCounterRepository
import com.walkingstepcounter.room.StepCounterEntity
import com.walkingstepcounter.room.TimerState
import com.walkingstepcounter.util.getCurrentDate
import com.walkingstepcounter.util.getCurrentDayOfWeek
import com.yourapp.sensor.StepCounterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StepCounterViewModel  @Inject constructor(private val stepCounterService: StepCounterService,
    private val repository: StepCounterRepository
    ) : ViewModel() {

    private val _sensorAvailable = MutableLiveData<Boolean>()
    val sensorAvailable: LiveData<Boolean> get() = _sensorAvailable

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> get() = _stepCount

    private var _stepCounterRowAvailable = MutableLiveData<Boolean>()
    val stepCounterRowAvailable : LiveData<Boolean> get() = _stepCounterRowAvailable


    init {
        _sensorAvailable.value = stepCounterService.isSensorAvailable()

        stepCounterRowChecking(1)
        // Observe the current timer state when ViewModel is initialized
        loadTimerState()
    }


    fun stepCounterRowChecking(id: Int)
    {
        viewModelScope.launch {
            _stepCounterRowAvailable.value = repository.isRowExisted(id)
        }
    }


    fun startStepCounting() {
        stepCounterService.startStepCounting { count ->
            Log.d("test", "startStepCounting: sensor : $count")
            _stepCount.value  = count
        }
    }

    fun stopStepCounting() {
        stepCounterService.stopStepCounting()
    }

    fun resetStepCounting()
    {
        stepCounterService.resetStepCounting()
        updateCurrentNumOfSteps(0)
    }



    fun resetCurrentNumOfSteps(step : Int)
    {
        viewModelScope.launch {
            repository.resetCurrentNumStepCounter(step)
        }
    }




    fun handleDateChange() {

        viewModelScope.launch {
            val storedDate = repository.getStoredCurrentDate()
            val currentDate = getCurrentDate()

            if (storedDate != currentDate) {
                // Reset steps if the date has changed
//                resetStepCounting()
                resetCurrentNumOfSteps(0)
                // Reset the timer to start from zero

            }
        }
    }





    val allStepCounterList = repository.getAllStepCounters()


    fun insertStepCounter(totalNum : Int, currentNum : Int)
    {
        viewModelScope.launch {
            repository.insertStepCounter(totalNum, currentNum, getCurrentDate(), getCurrentDayOfWeek())
        }
    }

    fun updateStepCounter(stepCounterEntity : StepCounterEntity)
    {
        viewModelScope.launch {
            repository.updateStepCounter(stepCounterEntity)
        }
    }

    fun deleteStepCounter(stepCounterEntity: StepCounterEntity)
    {
        viewModelScope.launch {
            repository.deleteStepCounterEntity(stepCounterEntity)
        }
    }

    fun getStepCounterById(id : Int) : LiveData<StepCounterEntity?>
    {
        val _stepCounterById = MutableLiveData<StepCounterEntity?>()
        viewModelScope.launch {
            _stepCounterById.postValue(repository.getStepCounterById(id).value)
        }
        return _stepCounterById
    }


    private val _currentNumOfStep = MutableLiveData<Int>()
    val currentNumOfStep: LiveData<Int> get() = _currentNumOfStep

    fun loadCurrentNumOfStep(id: Int) {
        viewModelScope.launch {
            repository.getCurrentNumOfStep(id).collect{
                _currentNumOfStep.value = it
                Log.d("stepViewModel", "loadCurrentNumOfStep: "+it)
            }
        }
    }

    private val _totalNumOfStep = MutableLiveData<Int>()
    val totalNumOfStep: LiveData<Int> get() = _totalNumOfStep

    fun loadTotalNumOfStep(id: Int) {
        viewModelScope.launch {
            repository.getTotalNumOfStep(id).collect{
                _totalNumOfStep.value = it
                Log.d("stepViewModel", "loadTotalNumOfStep: "+it)
            }
        }
    }


    fun updateCurrentNumOfSteps(step : Int)
    {
        viewModelScope.launch {
            repository.updateOrInsertCurrentStepCounter(step)
        }
    }


    // Function to update total number of steps
    fun updateTotalSteps(totalSteps: Int) {
        viewModelScope.launch {
            repository.updateTotalSteps(totalSteps)
//            resetStepCounting()
            resetCurrentNumOfSteps(0)
        }
    }


    fun updateAge(age: Int)
    {
        viewModelScope.launch {
            repository.updateAge(1, age)
        }
    }



    private val _weeklySteps = MutableLiveData<List<Pair<String, Int>>>()
    val weeklySteps: LiveData<List<Pair<String, Int>>> get() = _weeklySteps

    fun loadWeeklySteps() {
        viewModelScope.launch {
            val weeklyData = repository.getWeeklySteps()
            _weeklySteps.postValue(weeklyData)
        }
    }


    fun updateCurrentDate(id: Int, currentDate: String) {
        viewModelScope.launch {
            repository.updateCurrentDate(id, currentDate)
        }
    }


    private var _calries = MutableLiveData<Double>()
    val calries get() = _calries
    fun getCalriesBurned()
    {
        /*viewModelScope.launch {
            _calries.value = repository.getCaloriesBurned()
        }*/

        // Collect the Flow in a coroutine
        viewModelScope.launch {
            repository.getCaloriesBurned().collect { calories1 ->
                // Update UI with the latest calories burned value
                _calries.value = calories1
            }

        }
    }



    private var _distance = MutableLiveData<Double>()
    val distance get() = _distance

    fun getDistance()
    {
        // Collect the Flow in a coroutine
        viewModelScope.launch {
            repository.calculateDistance().collect { distance1 ->
                // Update UI with the latest calories burned value
                _distance.value = distance1
            }

        }
    }


    /*------------------------------------------------------time feature-------------------------------------------------------*/


    // LiveData that will observe the current timer state
    private val _timerState: MutableLiveData<TimerState> = MutableLiveData()
    val timerState: LiveData<TimerState> get() = _timerState

    // Timer Job for running the timer
    private var timerJob: Job? = null
    private var startTime: Long = 0L



    // Function to load current timer state from repository
    private fun loadTimerState() {
        // Observe the TimerState LiveData from the repository
        repository.getTimerStateById(1).observeForever { timerState ->
            _timerState.value = timerState
        }
    }
/*
    // Function to start or resume the timer
    fun startOrResumeTimer() {
        Log.d("timer", "startOrResumeTimer: viewmodel fired")
        val currentTimerState = _timerState.value
        if (currentTimerState != null && currentTimerState.timerState == "paused") {
            // Resume the timer from the last stopped point
            startTime = 0
            startTime = System.currentTimeMillis() - currentTimerState.timeSpent
             updateTimerState("running")
            Log.d("timer", "startOrResumeTimer: viewmodel fired")

        } else {
            // Start a new timer
            startTime = 0
            startTime = System.currentTimeMillis()
            updateTimerState("running")
        }

        startTimer()
    }*/


    // Function to start or resume the timer
    fun startOrResumeTimer() {
        Log.d("timer", "startOrResumeTimer: viewmodel fired")
        var currentTimerState = _timerState.value

        if (currentTimerState != null && currentTimerState.timerState == "paused") {
            // Resume the timer from the last stopped point only once
            startTime = System.currentTimeMillis() - currentTimerState.timeSpent
            Log.d("abbaskhan", "startOrResumeTimer: ${startTime}")
            updateTimerState("running", startTime)
            Log.d("timer", "Resuming timer from paused state at ${currentTimerState.timeSpent} ms")

        } else {
            // Start a new timer
            startTime = System.currentTimeMillis()
            Log.d("abbaskhan", "startOrResumeTimer: ${startTime}")
            updateTimerState("running", startTime)
            Log.d("timer", "Starting a new timer")
        }

        // Start or resume the timer without recalculating paused time repeatedly
        startTimer()
    }



/*    // Function to pause the timer
    fun pauseTimer() {
        timerJob?.cancel() // Stop the timer
        val elapsed = System.currentTimeMillis() - startTime
        val updatedTimeSpent = (_timerState.value?.timeSpent ?: 0L) + elapsed

        viewModelScope.launch {
            repository.updateTimerState(1, updatedTimeSpent, "paused")
            _timerState.value = TimerState(updatedTimeSpent, "paused")
        }
    }*/


    // Function to pause the timer
    fun pauseTimer() {
        timerJob?.cancel() // Stop the timer coroutine

        // Calculate the elapsed time since the last start or resume
//        val elapsed = System.currentTimeMillis() - startTime
        // Add this elapsed time to any previously accumulated time
        val updatedTimeSpent = (_timerState.value?.timeSpent ?: 0L)

        // Update the repository and state to reflect the paused timer
        viewModelScope.launch {
            repository.updateTimerState(1, updatedTimeSpent, "paused")
            _timerState.value = TimerState(updatedTimeSpent, "paused")
        }
    }



    /*
        // Private function to start the timer coroutine
        private fun startTimer() {
            timerJob?.cancel()  // Cancel any existing timer job

            timerJob = viewModelScope.launch {
                while (true) {
                    val elapsed = System.currentTimeMillis() - startTime
                    _timerState.postValue(TimerState(elapsed + (_timerState.value?.timeSpent ?: 0L), "running"))
                    delay(1000)  // Update every second
                }
            }
        }*/



    // Private function to start the timer coroutine
    private fun startTimer() {
        timerJob?.cancel()  // Cancel any existing timer job

        // Calculate base elapsed time once to prevent accumulation
        val baseElapsedTime = _timerState.value?.timeSpent ?: 0L
        Log.d("timer", "baseElapsedTime $baseElapsedTime")

        timerJob = viewModelScope.launch {
            while (true) {
//                val elapsed = System.currentTimeMillis() - startTime + baseElapsedTime
                val elapsed = System.currentTimeMillis() - baseElapsedTime + startTime


                Log.d("timer", "System.currentTimeMillis() ${System.currentTimeMillis()}")
                Log.d("timer", "baseElapsedTime $baseElapsedTime")
                Log.d("timer", "startTime $startTime")
                Log.d("timer", "elapsed $elapsed")

                _timerState.postValue(TimerState(elapsed, "running"))
                delay(1000)  // Update every second
            }
        }
    }


    // Helper function to update the timer state in the database and in LiveData
    private fun updateTimerState(state: String, time : Long) {

        Log.d("timer", "updateTimerState: $state")

        val currentTimerState = _timerState.value ?: TimerState(0L, state)

        viewModelScope.launch {
            repository.updateTimerState(1, time, state)
            _timerState.postValue(currentTimerState.copy(timeSpent = time, timerState = state))
        }
    }
/*


    // Helper function to update the timer state in the database and in LiveData
    private fun updateTimerState(state: String) {

        Log.d("timer", "updateTimerState: $state")

        val currentTimerState = _timerState.value ?: TimerState(0L, state)

        viewModelScope.launch {
            repository.updateTimerState(1, currentTimerState.timeSpent, state)
            _timerState.postValue(currentTimerState.copy(timerState = state))
        }
    }
*/

    // Function to reset the timer
    fun resetTimer() {
        timerJob?.cancel() // Stop any running timer job
        viewModelScope.launch {
            repository.resetTimer(1, 0L, "paused")
            _timerState.postValue(TimerState(0L, "paused"))
        }
    }


}