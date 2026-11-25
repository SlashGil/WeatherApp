package com.slashgil.weather.data.mapper

import com.slashgil.weather.core.util.DateFormatter
import com.slashgil.weather.core.util.WmoWeatherCodeMapper
import com.slashgil.weather.data.local.entity.ABTestEntity
import com.slashgil.weather.data.local.entity.DailyForecastEntity
import com.slashgil.weather.data.local.entity.ForecastEntity
import com.slashgil.weather.data.local.entity.ForecastWithDailyEntity
import com.slashgil.weather.data.remote.dto.ForecastResponseDto
import com.slashgil.weather.domain.model.ABTestFlag
import com.slashgil.weather.domain.model.DailyForecast
import com.slashgil.weather.domain.model.WeeklyForecast

fun ForecastResponseDto.toForecastEntity(cityName: String? = null, timestamp: Long = System.currentTimeMillis()): ForecastEntity {
    return ForecastEntity(
        id = 1,
        cityName = cityName,
        latitude = latitude,
        longitude = longitude,
        timezone = timezone ?: "auto",
        lastUpdatedTimestamp = timestamp
    )
}

fun ForecastResponseDto.toDailyForecastEntities(forecastOwnerId: Int = 1): List<DailyForecastEntity> {
    val dailyDto = daily ?: return emptyList()
    val list = mutableListOf<DailyForecastEntity>()
    val size = dailyDto.time.size
    for (i in 0 until size) {
        list.add(
            DailyForecastEntity(
                forecastOwnerId = forecastOwnerId,
                date = dailyDto.time.getOrNull(i) ?: "",
                weatherCode = dailyDto.weatherCode.getOrNull(i) ?: 0,
                tempMax = dailyDto.tempMax.getOrNull(i) ?: 0.0,
                tempMin = dailyDto.tempMin.getOrNull(i) ?: 0.0,
                precipitationProbabilityMax = dailyDto.precipitationProbabilityMax.getOrNull(i) ?: 0
            )
        )
    }
    return list
}

fun ForecastWithDailyEntity.toWeeklyForecast(): WeeklyForecast {
    return WeeklyForecast(
        cityName = forecast.cityName,
        latitude = forecast.latitude,
        longitude = forecast.longitude,
        timezone = forecast.timezone,
        lastUpdatedTimestamp = forecast.lastUpdatedTimestamp,
        dailyForecasts = dailyForecasts.map { it.toDailyForecast() }
    )
}

fun DailyForecastEntity.toDailyForecast(): DailyForecast {
    return DailyForecast(
        date = date,
        formattedDate = DateFormatter.formatDateString(date),
        weatherCode = weatherCode,
        weatherDescription = WmoWeatherCodeMapper.getWeatherDescription(weatherCode),
        weatherIcon = WmoWeatherCodeMapper.getWeatherIcon(weatherCode),
        tempMax = tempMax,
        tempMin = tempMin,
        precipitationProbabilityMax = precipitationProbabilityMax
    )
}

fun ABTestEntity.toDomain(): ABTestFlag = ABTestFlag(name = name, value = value)
