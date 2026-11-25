package com.slashgil.weather.presentation.main

import com.slashgil.weather.domain.model.ABTestFlag
import com.slashgil.weather.domain.model.DailyForecast
import com.slashgil.weather.domain.model.WeeklyForecast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherUiStateTest {

    private val sampleForecast = WeeklyForecast(
        cityName = "New York, NY",
        latitude = 40.7128,
        longitude = -74.0060,
        timezone = "America/New_York",
        lastUpdatedTimestamp = 1000L,
        dailyForecasts = listOf(
            DailyForecast("2026-11-24", "Tue, Nov 24", 0, "Clear sky", "☀️", 20.0, 10.0, 0)
        )
    )

    @Test
    fun `default WeatherUiState properties are initialized correctly`() {
        val state = WeatherUiState()

        assertNull(state.forecast)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.isDeveloperModeEnabled)
        assertTrue(state.abTests.isEmpty())
        assertFalse(state.showCoordsModal)
        assertFalse(state.showCityModal)
        assertNull(state.modalErrorMessage)
        assertFalse(state.isModalLoading)
    }

    @Test
    fun `copying WeatherUiState with populated forecast updates forecast and preserves state`() {
        val initial = WeatherUiState(isDeveloperModeEnabled = true)
        val updated = initial.copy(forecast = sampleForecast)

        assertEquals(sampleForecast, updated.forecast)
        assertTrue(updated.isDeveloperModeEnabled)
    }

    @Test
    fun `copying WeatherUiState with modal visibility updates modal flags`() {
        val initial = WeatherUiState()
        val withCoordsModal = initial.copy(showCoordsModal = true)
        val withCityModal = initial.copy(showCityModal = true)

        assertTrue(withCoordsModal.showCoordsModal)
        assertFalse(withCoordsModal.showCityModal)

        assertTrue(withCityModal.showCityModal)
        assertFalse(withCityModal.showCoordsModal)
    }

    @Test
    fun `copying WeatherUiState with ABTest flags updates abTests list`() {
        val flags = listOf(
            ABTestFlag("put_default_city", true),
            ABTestFlag("give_coords", false),
            ABTestFlag("select_city", false)
        )
        val state = WeatherUiState(abTests = flags)

        assertEquals(3, state.abTests.size)
        assertTrue(state.abTests.first().value)
    }
}
