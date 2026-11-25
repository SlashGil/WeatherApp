package com.slashgil.weather.domain.strategy

import android.content.Context
import android.location.Geocoder
import com.slashgil.weather.core.location.LocationTracker
import com.slashgil.weather.domain.model.Coordinates
import com.slashgil.weather.domain.model.LocationResolutionResult
import java.util.Locale

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
    private val locationTracker: LocationTracker,
    private val context: Context
) : LocationResolutionStrategy {
    override suspend fun resolve(): LocationResolutionResult {
        val location = locationTracker.getCurrentLocation()
        return if (location != null) {
            val locationName = getCityNameFromCoordinates(context, location.latitude, location.longitude)
            LocationResolutionResult.Success(
                coordinates = location,
                locationName = locationName
            )
        } else {
            LocationResolutionResult.Success(
                coordinates = DefaultCityConfig.COORDINATES,
                locationName = "${DefaultCityConfig.NAME} (Fallback)"
            )
        }
    }

    private fun getCityNameFromCoordinates(context: Context, lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val address = addresses?.firstOrNull()
            if (address != null) {
                val city = address.locality ?: address.subAdminArea ?: address.subLocality
                val state = address.adminArea
                val country = address.countryCode
                val formatted = listOfNotNull(city, state, country)
                    .distinct()
                    .joinToString(", ")
                formatted.ifBlank { "GPS Location" }
            } else {
                "GPS Location"
            }
        } catch (e: Exception) {
            "GPS Location"
        }
    }
}
