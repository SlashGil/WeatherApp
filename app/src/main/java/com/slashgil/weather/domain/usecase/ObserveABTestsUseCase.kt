package com.slashgil.weather.domain.usecase

import com.slashgil.weather.domain.model.ABTestFlag
import com.slashgil.weather.domain.repository.ABTestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveABTestsUseCase @Inject constructor(
    private val repository: ABTestRepository
) {
    operator fun invoke(): Flow<List<ABTestFlag>> = repository.observeABTests()
}
