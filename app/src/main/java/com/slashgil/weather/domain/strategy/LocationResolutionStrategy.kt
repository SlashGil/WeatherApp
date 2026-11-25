package com.slashgil.weather.domain.strategy

import com.slashgil.weather.core.location.LocationTracker
import com.slashgil.weather.domain.model.Coordinates
import com.slashgil.weather.domain.model.LocationResolutionResult

interface LocationResolutionStrategy {
    suspend fun resolve(): LocationResolutionResult
}

object DefaultCityConfig {
    const val NAME = "New York, NY"
    val COORDINATES = Coordinates(40.7128, -74.0060)
}

class DefaultCityStrategy : LocationResolutionStrategy {
    override suspend fun resolve(): LocationResolutionResult {
        return LocationResolutionResult.Success(
            coordinates = DefaultCityConfig.COORDINATES,
            locationName = DefaultCityConfig.NAME
        )
    }
}

class ManualCoordsStrategy : LocationResolutionStrategy {
    override suspend fun resolve(): LocationResolutionResult {
        return LocationResolutionResult.RequiresCoordsInput
    }
}

class ManualCityStrategy : LocationResolutionStrategy {
    override suspend fun resolve(): LocationResolutionResult {
        return LocationResolutionResult.RequiresCityInput
    }
}

class DeviceGpsStrategy(
    private val locationTracker: LocationTracker
) : LocationResolutionStrategy {
    override suspend fun resolve(): LocationResolutionResult {
        val location = locationTracker.getCurrentLocation()
        return if (location != null) {
            LocationResolutionResult.Success(coordinates = location)
        } else {
            LocationResolutionResult.Success(
                coordinates = DefaultCityConfig.COORDINATES,
                locationName = "${DefaultCityConfig.NAME} (Fallback)"
            )
        }
    }
}
