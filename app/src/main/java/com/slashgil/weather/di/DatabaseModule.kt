package com.slashgil.weather.di

import android.content.Context
import androidx.room.Room
import com.slashgil.weather.data.local.WeatherDatabase
import com.slashgil.weather.data.local.dao.ABTestDao
import com.slashgil.weather.data.local.dao.ForecastDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWeatherDatabase(
        @ApplicationContext context: Context
    ): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "forecast_database"
        )
            .addCallback(WeatherDatabase.CALLBACK)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideForecastDao(database: WeatherDatabase): ForecastDao {
        return database.forecastDao()
    }

    @Provides
    @Singleton
    fun provideABTestDao(database: WeatherDatabase): ABTestDao {
        return database.abTestDao()
    }
}
