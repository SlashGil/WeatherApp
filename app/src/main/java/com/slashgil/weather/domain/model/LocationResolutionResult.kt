package com.slashgil.weather.domain.model

sealed interface LocationResolutionResult {
    data class Success(val coordinates: Coordinates, val locationName: String? = null) : LocationResolutionResult
    data object RequiresCoordsInput : LocationResolutionResult
    data object RequiresCityInput : LocationResolutionResult
    data class Error(val message: String) : LocationResolutionResult
}
