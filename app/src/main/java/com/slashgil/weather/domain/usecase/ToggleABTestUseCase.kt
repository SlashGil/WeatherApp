package com.slashgil.weather.domain.usecase

import com.slashgil.weather.domain.repository.ABTestRepository
import javax.inject.Inject

class ToggleABTestUseCase @Inject constructor(
    private val repository: ABTestRepository
) {
    private val allFlags = listOf("put_default_city", "give_coords", "select_city")

    suspend operator fun invoke(targetName: String, newValue: Boolean) {
        if (newValue) {
            for (flagName in allFlags) {
                repository.updateABTest(flagName, flagName == targetName)
            }
        } else {
            repository.updateABTest(targetName, false)
        }
    }
}
