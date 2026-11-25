package com.slashgil.weather.domain.strategy

import com.slashgil.weather.core.location.LocationTracker
import com.slashgil.weather.domain.model.ABTestFlag
import javax.inject.Inject

class LocationStrategyFactory @Inject constructor(
    private val locationTracker: LocationTracker
) {
    fun createStrategy(abTests: List<ABTestFlag>): LocationResolutionStrategy {
        val putDefaultCity = abTests.find { it.name == "put_default_city" }?.value == true
        val giveCoords = abTests.find { it.name == "give_coords" }?.value == true
        val selectCity = abTests.find { it.name == "select_city" }?.value == true

        return when {
            putDefaultCity -> DefaultCityStrategy()
            giveCoords -> ManualCoordsStrategy()
            selectCity -> ManualCityStrategy()
            else -> DeviceGpsStrategy(locationTracker)
        }
    }
}
