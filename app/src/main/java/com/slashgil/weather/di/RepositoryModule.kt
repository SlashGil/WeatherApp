package com.slashgil.weather.di

import com.slashgil.weather.data.repository.ABTestRepositoryImpl
import com.slashgil.weather.data.repository.WeatherRepositoryImpl
import com.slashgil.weather.domain.repository.ABTestRepository
import com.slashgil.weather.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        weatherRepositoryImpl: WeatherRepositoryImpl
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindABTestRepository(
        abTestRepositoryImpl: ABTestRepositoryImpl
    ): ABTestRepository
}
