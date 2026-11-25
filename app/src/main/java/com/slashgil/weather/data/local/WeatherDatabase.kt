package com.slashgil.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.slashgil.weather.data.local.dao.ABTestDao
import com.slashgil.weather.data.local.dao.ForecastDao
import com.slashgil.weather.data.local.entity.ABTestEntity
import com.slashgil.weather.data.local.entity.DailyForecastEntity
import com.slashgil.weather.data.local.entity.ForecastEntity
import java.util.concurrent.Executors

@Database(
    entities = [
        ForecastEntity::class,
        DailyForecastEntity::class,
        ABTestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun forecastDao(): ForecastDao
    abstract fun abTestDao(): ABTestDao

    companion object {
        val CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Executors.newSingleThreadExecutor().execute {
                    db.execSQL("INSERT OR REPLACE INTO ab_tests (name, value) VALUES ('put_default_city', 0)")
                    db.execSQL("INSERT OR REPLACE INTO ab_tests (name, value) VALUES ('give_coords', 0)")
                    db.execSQL("INSERT OR REPLACE INTO ab_tests (name, value) VALUES ('select_city', 0)")
                }
            }
        }
    }
}
