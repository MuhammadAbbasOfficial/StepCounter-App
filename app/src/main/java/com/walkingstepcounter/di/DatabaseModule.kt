package com.walkingstepcounter.di

import android.app.Application
import androidx.room.Room
import com.walkingstepcounter.room.StepCounterDAO
import com.walkingstepcounter.room.StepCounterDatabase
import com.walkingstepcounter.room.StepCounterEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app : Application) : StepCounterDatabase{
        return Room.databaseBuilder(app, StepCounterDatabase::class.java, "step_counter_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideStepCounterDao(db : StepCounterDatabase) : StepCounterDAO
    {
        return db.stepCounterDAO()
    }

}