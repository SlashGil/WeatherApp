package com.slashgil.weather.presentation.main

import com.slashgil.weather.domain.model.ABTestFlag
import com.slashgil.weather.domain.model.WeeklyForecast

data class WeatherUiState(
    val forecast: WeeklyForecast? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDeveloperModeEnabled: Boolean = false,
    val abTests: List<ABTestFlag> = emptyList(),
    val showCoordsModal: Boolean = false,
    val showCityModal: Boolean = false,
    val modalErrorMessage: String? = null,
    val isModalLoading: Boolean = false
)
