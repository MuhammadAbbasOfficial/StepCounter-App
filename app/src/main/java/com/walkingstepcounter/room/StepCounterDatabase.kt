package com.walkingstepcounter.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [StepCounterEntity::class], version = 5)
abstract class StepCounterDatabase : RoomDatabase(){

    abstract fun stepCounterDAO() : StepCounterDAO

}