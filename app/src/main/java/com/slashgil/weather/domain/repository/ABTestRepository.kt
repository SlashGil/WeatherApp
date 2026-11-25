package com.slashgil.weather.domain.repository

import com.slashgil.weather.domain.model.ABTestFlag
import kotlinx.coroutines.flow.Flow

interface ABTestRepository {
    fun observeABTests(): Flow<List<ABTestFlag>>
    suspend fun getABTest(name: String): ABTestFlag?
    suspend fun updateABTest(name: String, value: Boolean)
}
