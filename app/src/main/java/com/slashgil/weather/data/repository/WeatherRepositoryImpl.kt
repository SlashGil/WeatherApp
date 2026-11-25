package com.slashgil.weather.data.repository

import com.slashgil.weather.data.local.dao.ForecastDao
import com.slashgil.weather.data.mapper.toDailyForecastEntities
import com.slashgil.weather.data.mapper.toForecastEntity
import com.slashgil.weather.data.mapper.toWeeklyForecast
import com.slashgil.weather.data.remote.api.OpenMeteoForecastApi
import com.slashgil.weather.data.remote.api.OpenMeteoGeocodingApi
import com.slashgil.weather.data.remote.dto.GeocodingResultDto
import com.slashgil.weather.domain.model.WeeklyForecast
import com.slashgil.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val forecastApi: OpenMeteoForecastApi,
    private val geocodingApi: OpenMeteoGeocodingApi,
    private val forecastDao: ForecastDao
) : WeatherRepository {

    override fun observeCachedForecast(): Flow<WeeklyForecast?> {
        return forecastDao.getForecastWithDailyFlow().map { it?.toWeeklyForecast() }
    }

    override suspend fun refreshForecast(
        latitude: Double,
        longitude: Double,
        cityName: String?
    ): Result<WeeklyForecast> {
        return try {
            val responseDto = forecastApi.getWeeklyForecast(latitude = latitude, longitude = longitude)
            val now = System.currentTimeMillis()
            val forecastEntity = responseDto.toForecastEntity(cityName = cityName, timestamp = now)
            val dailyEntities = responseDto.toDailyForecastEntities(forecastOwnerId = forecastEntity.id)
            forecastDao.saveForecast(forecastEntity, dailyEntities)

            val updatedForecastWithDaily = com.slashgil.weather.data.local.entity.ForecastWithDailyEntity(
                forecast = forecastEntity,
                dailyForecasts = dailyEntities
            )
            Result.success(updatedForecastWithDaily.toWeeklyForecast())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchCity(cityName: String): Result<List<GeocodingResultDto>> {
        return try {
            val response = geocodingApi.searchCity(name = cityName)
            Result.success(response.results ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
