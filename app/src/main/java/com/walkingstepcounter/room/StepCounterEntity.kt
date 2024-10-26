package com.walkingstepcounter.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "StepCounter")
data class StepCounterEntity(
    @PrimaryKey
    val id : Int = 1 ,
    val age : Int,
    val totalNumOfSteps : Int,
    val currentNumOfStep : Int,
    val currentDate : String,
    val previousDate : String,
    val currentDay : String,
    val monday : Int,
    val tuesday : Int,
    val wednesday : Int,
    val thursday : Int,
    val friday : Int,
    val saturday : Int,
    val sunday : Int,
    val timeSpent: Long = 0L,  // Time in milliseconds
    val timerState: String = "paused"  // Either "paused" or "running"

)