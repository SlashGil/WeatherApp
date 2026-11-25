package com.slashgil.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.slashgil.weather.data.local.entity.DailyForecastEntity
import com.slashgil.weather.data.local.entity.ForecastEntity
import com.slashgil.weather.data.local.entity.ForecastWithDailyEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ForecastDao {
    @Transaction
    @Query("SELECT * FROM forecasts WHERE id = 1")
    abstract fun getForecastWithDailyFlow(): Flow<ForecastWithDailyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertForecast(forecast: ForecastEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDailyForecasts(dailyForecasts: List<DailyForecastEntity>)

    @Query("DELETE FROM forecasts")
    abstract suspend fun clearForecasts()

    @Query("DELETE FROM daily_forecasts")
    abstract suspend fun clearDailyForecasts()

    @Transaction
    open suspend fun saveForecast(forecast: ForecastEntity, dailyForecasts: List<DailyForecastEntity>) {
        clearDailyForecasts()
        clearForecasts()
        insertForecast(forecast)
        insertDailyForecasts(dailyForecasts)
    }
}
