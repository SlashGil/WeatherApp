package com.slashgil.weather.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slashgil.weather.domain.model.Coordinates
import com.slashgil.weather.domain.model.LocationResolutionResult
import com.slashgil.weather.domain.usecase.ObserveABTestsUseCase
import com.slashgil.weather.domain.usecase.ObserveCachedForecastUseCase
import com.slashgil.weather.domain.usecase.RefreshWeeklyForecastUseCase
import com.slashgil.weather.domain.usecase.ResolveCityCoordsUseCase
import com.slashgil.weather.domain.usecase.WeeklyForecastResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val observeCachedForecastUseCase: ObserveCachedForecastUseCase,
    private val observeABTestsUseCase: ObserveABTestsUseCase,
    private val refreshWeeklyForecastUseCase: RefreshWeeklyForecastUseCase,
    private val resolveCityCoordsUseCase: ResolveCityCoordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        observeCachedForecast()
        observeABTests()
    }

    private fun observeCachedForecast() {
        viewModelScope.launch {
            observeCachedForecastUseCase().collectLatest { cachedForecast ->
                _uiState.update { it.copy(forecast = cachedForecast) }
            }
        }
    }

    private fun observeABTests() {
        viewModelScope.launch {
            observeABTestsUseCase().collectLatest { flags ->
                _uiState.update { it.copy(abTests = flags) }
            }
        }
    }

    fun refreshForecast(
        manualCoordinates: Coordinates? = null,
        manualLocationName: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val currentAbTests = _uiState.value.abTests

            refreshWeeklyForecastUseCase(
                abTests = currentAbTests,
                manualCoordinates = manualCoordinates,
                manualLocationName = manualLocationName
            ).fold(
                onSuccess = { result ->
                    when (result) {
                        is WeeklyForecastResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = null,
                                    showCoordsModal = false,
                                    showCityModal = false,
                                    modalErrorMessage = null
                                )
                            }
                        }
                        is WeeklyForecastResult.RequiresCoordsInput -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    showCoordsModal = true
                                )
                            }
                        }
                        is WeeklyForecastResult.RequiresCityInput -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    showCityModal = true
                                )
                            }
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to refresh weather forecast"
                        )
                    }
                }
            )
        }
    }

    fun submitCoordinates(latStr: String, lonStr: String) {
        val lat = latStr.toDoubleOrNull()
        val lon = lonStr.toDoubleOrNull()

        if (lat == null || lat < -90.0 || lat > 90.0) {
            _uiState.update { it.copy(modalErrorMessage = "Latitude must be between -90 and 90") }
            return
        }
        if (lon == null || lon < -180.0 || lon > 180.0) {
            _uiState.update { it.copy(modalErrorMessage = "Longitude must be between -180 and 180") }
            return
        }

        _uiState.update { it.copy(modalErrorMessage = null) }
        refreshForecast(
            manualCoordinates = Coordinates(lat, lon),
            manualLocationName = "Custom Coords ($lat, $lon)"
        )
    }

    fun submitCityQuery(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isModalLoading = true, modalErrorMessage = null) }
            when (val resolution = resolveCityCoordsUseCase(query)) {
                is LocationResolutionResult.Success -> {
                    _uiState.update { it.copy(isModalLoading = false) }
                    refreshForecast(
                        manualCoordinates = resolution.coordinates,
                        manualLocationName = resolution.locationName
                    )
                }
                is LocationResolutionResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isModalLoading = false,
                            modalErrorMessage = resolution.message
                        )
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            isModalLoading = false,
                            modalErrorMessage = "Unable to resolve city"
                        )
                    }
                }
            }
        }
    }

    fun enableDeveloperMode() {
        _uiState.update { it.copy(isDeveloperModeEnabled = true) }
    }

    fun dismissCoordsModal() {
        _uiState.update { it.copy(showCoordsModal = false, modalErrorMessage = null) }
    }

    fun dismissCityModal() {
        _uiState.update { it.copy(showCityModal = false, modalErrorMessage = null) }
    }

    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
