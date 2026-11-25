package com.slashgil.weather.data.repository

import com.slashgil.weather.data.local.dao.ABTestDao
import com.slashgil.weather.data.local.entity.ABTestEntity
import com.slashgil.weather.data.mapper.toDomain
import com.slashgil.weather.domain.model.ABTestFlag
import com.slashgil.weather.domain.repository.ABTestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ABTestRepositoryImpl @Inject constructor(
    private val abTestDao: ABTestDao
) : ABTestRepository {

    override fun observeABTests(): Flow<List<ABTestFlag>> {
        return abTestDao.getAllABTestsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getABTest(name: String): ABTestFlag? {
        return abTestDao.getABTestByName(name)?.toDomain()
    }

    override suspend fun updateABTest(name: String, value: Boolean) {
        abTestDao.updateABTest(ABTestEntity(name = name, value = value))
    }
}
