package com.walkingstepcounter.di

import android.content.Context
import com.yourapp.sensor.StepCounterService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    fun provideStepCounterService(
        @ApplicationContext context: Context
    ): StepCounterService {
        return StepCounterService(context)
    }

}