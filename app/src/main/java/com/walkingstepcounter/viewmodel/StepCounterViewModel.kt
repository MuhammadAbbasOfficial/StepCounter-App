package com.walkingstepcounter.viewmodel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingstepcounter.repository.StepCounterRepository
import com.walkingstepcounter.room.StepCounterEntity
import com.walkingstepcounter.util.getCurrentDate
import com.walkingstepcounter.util.getCurrentDayOfWeek
import com.yourapp.sensor.StepCounterService
import dagger.hilt.android.lifecycle.HiltViewModel
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
//        loadCurrentNumOfStep(1)
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





}