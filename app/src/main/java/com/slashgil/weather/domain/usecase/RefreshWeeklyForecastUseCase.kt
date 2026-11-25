package com.slashgil.weather.domain.usecase

import com.slashgil.weather.domain.model.ABTestFlag
import com.slashgil.weather.domain.model.Coordinates
import com.slashgil.weather.domain.model.LocationResolutionResult
import com.slashgil.weather.domain.model.WeeklyForecast
import com.slashgil.weather.domain.repository.WeatherRepository
import com.slashgil.weather.domain.strategy.LocationStrategyFactory
import javax.inject.Inject

sealed interface WeeklyForecastResult {
    data class Success(val forecast: WeeklyForecast) : WeeklyForecastResult
    data object RequiresCoordsInput : WeeklyForecastResult
    data object RequiresCityInput : WeeklyForecastResult
}

class RefreshWeeklyForecastUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationStrategyFactory: LocationStrategyFactory
) {
    suspend operator fun invoke(
        abTests: List<ABTestFlag>,
        manualCoordinates: Coordinates? = null,
        manualLocationName: String? = null
    ): Result<WeeklyForecastResult> {
        if (manualCoordinates != null) {
            return weatherRepository.refreshForecast(
                latitude = manualCoordinates.latitude,
                longitude = manualCoordinates.longitude,
                cityName = manualLocationName
            ).map { forecast ->
                WeeklyForecastResult.Success(forecast)
            }
        }

        val strategy = locationStrategyFactory.createStrategy(abTests)
        return when (val resolution = strategy.resolve()) {
            is LocationResolutionResult.Success -> {
                weatherRepository.refreshForecast(
                    latitude = resolution.coordinates.latitude,
                    longitude = resolution.coordinates.longitude,
                    cityName = resolution.locationName
                ).map { forecast ->
                    WeeklyForecastResult.Success(forecast)
                }
            }
            is LocationResolutionResult.RequiresCoordsInput -> {
                Result.success(WeeklyForecastResult.RequiresCoordsInput)
            }
            is LocationResolutionResult.RequiresCityInput -> {
                Result.success(WeeklyForecastResult.RequiresCityInput)
            }
            is LocationResolutionResult.Error -> {
                Result.failure(Exception(resolution.message))
            }
        }
    }
}
