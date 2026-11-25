package com.slashgil.weather.domain.model

data class DailyForecast(
    val date: String,
    val formattedDate: String,
    val weatherCode: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationProbabilityMax: Int
)
