package com.slashgil.weather.domain.repository

import com.slashgil.weather.data.remote.dto.GeocodingResultDto
import com.slashgil.weather.domain.model.WeeklyForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeCachedForecast(): Flow<WeeklyForecast?>
    suspend fun refreshForecast(latitude: Double, longitude: Double, cityName: String? = null): Result<WeeklyForecast>
    suspend fun searchCity(cityName: String): Result<List<GeocodingResultDto>>
}
