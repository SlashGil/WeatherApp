package com.slashgil.weather.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ForecastWithDailyEntity(
    @Embedded val forecast: ForecastEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "forecastOwnerId"
    )
    val dailyForecasts: List<DailyForecastEntity>
)
