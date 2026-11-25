package com.slashgil.weather.domain.model

data class WeeklyForecast(
    val cityName: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val lastUpdatedTimestamp: Long,
    val dailyForecasts: List<DailyForecast>
)
