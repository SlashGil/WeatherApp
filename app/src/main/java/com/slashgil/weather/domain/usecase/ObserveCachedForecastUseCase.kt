package com.slashgil.weather.domain.usecase

import com.slashgil.weather.domain.model.WeeklyForecast
import com.slashgil.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCachedForecastUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(): Flow<WeeklyForecast?> = repository.observeCachedForecast()
}
