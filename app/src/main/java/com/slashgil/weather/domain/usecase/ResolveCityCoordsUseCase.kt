package com.slashgil.weather.domain.usecase

import com.slashgil.weather.domain.model.Coordinates
import com.slashgil.weather.domain.model.LocationResolutionResult
import com.slashgil.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class ResolveCityCoordsUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(query: String): LocationResolutionResult {
        if (query.isBlank()) {
            return LocationResolutionResult.Error("City name cannot be empty")
        }

        val parts = query.split(",").map { it.trim() }
        val cityName = parts.firstOrNull() ?: query

        return weatherRepository.searchCity(cityName).fold(
            onSuccess = { results ->
                if (results.isEmpty()) {
                    LocationResolutionResult.Error("No results found for '$query'")
                } else {
                    val match = if (parts.size > 1) {
                        results.find { item ->
                            parts.any { p ->
                                item.name.contains(p, ignoreCase = true) ||
                                        (item.admin1?.contains(p, ignoreCase = true) == true) ||
                                        (item.country?.contains(p, ignoreCase = true) == true)
                            }
                        } ?: results.first()
                    } else {
                        results.first()
                    }

                    val displayName = listOfNotNull(match.name, match.admin1, match.country)
                        .distinct()
                        .joinToString(", ")

                    LocationResolutionResult.Success(
                        coordinates = Coordinates(match.latitude, match.longitude),
                        locationName = displayName
                    )
                }
            },
            onFailure = { error ->
                LocationResolutionResult.Error(error.message ?: "Failed to resolve city coordinates")
            }
        )
    }
}
