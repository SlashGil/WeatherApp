package com.slashgil.weather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forecasts")
data class ForecastEntity(
    @PrimaryKey val id: Int = 1,
    val cityName: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val lastUpdatedTimestamp: Long
)
