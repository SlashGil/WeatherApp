package com.slashgil.weather.presentation.main

import app.cash.turbine.test
import com.slashgil.weather.domain.model.DailyForecast
import com.slashgil.weather.domain.model.WeeklyForecast
import com.slashgil.weather.domain.usecase.ObserveABTestsUseCase
import com.slashgil.weather.domain.usecase.ObserveCachedForecastUseCase
import com.slashgil.weather.domain.usecase.RefreshWeeklyForecastUseCase
import com.slashgil.weather.domain.usecase.ResolveCityCoordsUseCase
import com.slashgil.weather.domain.usecase.WeeklyForecastResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeCachedForecastUseCase: ObserveCachedForecastUseCase = mockk()
    private val observeABTestsUseCase: ObserveABTestsUseCase = mockk()
    private val refreshWeeklyForecastUseCase: RefreshWeeklyForecastUseCase = mockk()
    private val resolveCityCoordsUseCase: ResolveCityCoordsUseCase = mockk()

    private lateinit var viewModel: WeatherViewModel

    private val mockWeeklyForecast = WeeklyForecast(
        cityName = "New York, NY",
        latitude = 40.7128,
        longitude = -74.0060,
        timezone = "America/New_York",
        lastUpdatedTimestamp = 1000L,
        dailyForecasts = listOf(
            DailyForecast(
                date = "2026-11-24",
                formattedDate = "Tue, Nov 24",
                weatherCode = 0,
                weatherDescription = "Clear sky",
                weatherIcon = "☀️",
                tempMax = 20.0,
                tempMin = 10.0,
                precipitationProbabilityMax = 0
            )
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeCachedForecastUseCase() } returns flowOf(mockWeeklyForecast)
        every { observeABTestsUseCase() } returns flowOf(emptyList())

        viewModel = WeatherViewModel(
            observeCachedForecastUseCase,
            observeABTestsUseCase,
            refreshWeeklyForecastUseCase,
            resolveCityCoordsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init collects cached forecast and updates uiState`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            val updatedState = awaitItem()
            assertEquals(mockWeeklyForecast, updatedState.forecast)
        }
    }

    @Test
    fun `enableDeveloperMode sets isDeveloperModeEnabled to true`() = runTest {
        assertFalse(viewModel.uiState.value.isDeveloperModeEnabled)
        viewModel.enableDeveloperMode()
        assertTrue(viewModel.uiState.value.isDeveloperModeEnabled)
    }

    @Test
    fun `refreshForecast failure retains cached forecast and sets non-blocking error message`() = runTest {
        coEvery {
            refreshWeeklyForecastUseCase(any(), any(), any())
        } returns Result.failure(Exception("Connection timeout"))

        viewModel.refreshForecast()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Connection timeout", state.errorMessage)
        assertEquals(mockWeeklyForecast, state.forecast)
        assertFalse(state.isLoading)
    }

    @Test
    fun `refreshForecast with give_coords active triggers showCoordsModal`() = runTest {
        coEvery {
            refreshWeeklyForecastUseCase(any(), any(), any())
        } returns Result.success(WeeklyForecastResult.RequiresCoordsInput)

        viewModel.refreshForecast()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showCoordsModal)
        assertFalse(state.isLoading)
    }

    @Test
    fun `refreshForecast with select_city active triggers showCityModal`() = runTest {
        coEvery {
            refreshWeeklyForecastUseCase(any(), any(), any())
        } returns Result.success(WeeklyForecastResult.RequiresCityInput)

        viewModel.refreshForecast()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showCityModal)
        assertFalse(state.isLoading)
    }
}
