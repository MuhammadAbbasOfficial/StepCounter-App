package com.walkingstepcounter.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StepCounterDAO {

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    suspend fun insertTotalNumOfStep(stepsModal : StepCounterEntity)

    @Query("SELECT * FROM StepCounter WHERE id = :id")
    fun selectStepCounterById (id : Int) : LiveData<StepCounterEntity?>

    @Query("SELECT * FROM StepCounter")
    fun getAllStepCounters(): LiveData<List<StepCounterEntity>>

    @Update
    suspend fun update(stepCounterEntity: StepCounterEntity)

    @Delete
    suspend fun delete(stepCounterEntity: StepCounterEntity)

    @Query("UPDATE StepCounter SET currentNumOfStep = :currentNumOfStep WHERE id = :id")
    suspend fun updateCurrentStep(id: Int, currentNumOfStep: Int)

    @Query("SELECT currentNumOfStep FROM StepCounter WHERE id = :id")
    fun getCurrentNumOfStepById(id: Int): Flow<Int?>
    @Query("SELECT totalNumOfSteps FROM StepCounter WHERE id = :id")
    fun getTotalNumOfStepById(id: Int): Flow<Int?>

    @Query("UPDATE StepCounter SET monday = :steps WHERE id = :id")
    suspend fun updateMondaySteps(id: Int, steps: Int)

    @Query("UPDATE StepCounter SET tuesday = :steps WHERE id = :id")
    suspend fun updateTuesdaySteps(id: Int, steps: Int)

    @Query("UPDATE StepCounter SET wednesday = :steps WHERE id = :id")
    suspend fun updateWednesdaySteps(id: Int, steps: Int)

    @Query("UPDATE StepCounter SET thursday = :steps WHERE id = :id")
    suspend fun updateThursdaySteps(id: Int, steps: Int)

    @Query("UPDATE StepCounter SET friday = :steps WHERE id = :id")
    suspend fun updateFridaySteps(id: Int, steps: Int)

    @Query("UPDATE StepCounter SET saturday = :steps WHERE id = :id")
    suspend fun updateSaturdaySteps(id: Int, steps: Int)

    @Query("UPDATE StepCounter SET sunday = :steps WHERE id = :id")
    suspend fun updateSundaySteps(id: Int, steps: Int)


    // GET methods for each day
    @Query("SELECT monday FROM StepCounter WHERE id = :id")
    suspend fun getMondaySteps(id: Int): Int?

    @Query("SELECT tuesday FROM StepCounter WHERE id = :id")
    suspend fun getTuesdaySteps(id: Int): Int?

    @Query("SELECT wednesday FROM StepCounter WHERE id = :id")
    suspend fun getWednesdaySteps(id: Int): Int?

    @Query("SELECT thursday FROM StepCounter WHERE id = :id")
    suspend fun getThursdaySteps(id: Int): Int?

    @Query("SELECT friday FROM StepCounter WHERE id = :id")
    suspend fun getFridaySteps(id: Int): Int?

    @Query("SELECT saturday FROM StepCounter WHERE id = :id")
    suspend fun getSaturdaySteps(id: Int): Int?

    @Query("SELECT sunday FROM StepCounter WHERE id = :id")
    suspend fun getSundaySteps(id: Int): Int?

    @Query("UPDATE StepCounter SET totalNumOfSteps = :totalNumOfSteps WHERE id = :id")
    suspend fun updateTotalNumOfSteps(id: Int, totalNumOfSteps: Int)

    @Query("SELECT currentDate FROM StepCounter WHERE id = :id")
    suspend fun getCurrentDateById(id: Int): String?


    @Query("SELECT EXISTS(SELECT 1 FROM StepCounter WHERE id = :id)")
    suspend fun isRowExisted(id: Int): Boolean


    @Query("UPDATE StepCounter SET currentDate = :currentDate WHERE id = :id")
    suspend fun updateCurrentDate(id: Int, currentDate: String)


    @Query("SELECT age FROM StepCounter WHERE id = :id")
    suspend fun getAgeById(id: Int): Int?

    @Query("UPDATE StepCounter SET age = :age WHERE id = :id")
    suspend fun updateAge(id: Int, age: Int)

    @Query("UPDATE StepCounter SET timeSpent = :timeSpent, timerState = :timerState WHERE id = :id")
    suspend fun updateTimerState11(id: Int, timeSpent: Long, timerState: String)

    @Query("UPDATE StepCounter SET timeSpent = :timeSpent, timerState = :timerState WHERE id = :id")
    suspend fun resetTimer(id: Int, timeSpent: Long, timerState: String)

    @Query("SELECT timeSpent, timerState FROM StepCounter WHERE id = :id")
    fun getTimerStateById(id: Int): LiveData<TimerState>

}