package com.slashgil.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.slashgil.weather.data.local.entity.ABTestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ABTestDao {
    @Query("SELECT * FROM ab_tests")
    fun getAllABTestsFlow(): Flow<List<ABTestEntity>>

    @Query("SELECT * FROM ab_tests WHERE name = :name")
    suspend fun getABTestByName(name: String): ABTestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertABTests(abTests: List<ABTestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateABTest(abTest: ABTestEntity)

    @Query("UPDATE ab_tests SET value = :value WHERE name = :name")
    suspend fun setABTestValue(name: String, value: Boolean)
}
