package com.slashgil.weather.domain.usecase

import com.slashgil.weather.core.location.LocationTracker
import com.slashgil.weather.domain.model.ABTestFlag
import com.slashgil.weather.domain.model.Coordinates
import com.slashgil.weather.domain.model.DailyForecast
import com.slashgil.weather.domain.model.WeeklyForecast
import com.slashgil.weather.domain.repository.WeatherRepository
import com.slashgil.weather.domain.strategy.DefaultCityConfig
import com.slashgil.weather.domain.strategy.LocationStrategyFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RefreshWeeklyForecastUseCaseTest {

    private val weatherRepository: WeatherRepository = mockk()
    private val locationTracker: LocationTracker = mockk()
    private lateinit var locationStrategyFactory: LocationStrategyFactory
    private lateinit var useCase: RefreshWeeklyForecastUseCase

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
        locationStrategyFactory = LocationStrategyFactory(locationTracker)
        useCase = RefreshWeeklyForecastUseCase(weatherRepository, locationStrategyFactory)
    }

    @Test
    fun `invoke with put_default_city active returns forecast for default city without checking GPS`() = runTest {
        val abTests = listOf(
            ABTestFlag("put_default_city", true),
            ABTestFlag("give_coords", false),
            ABTestFlag("select_city", false)
        )

        coEvery {
            weatherRepository.refreshForecast(
                latitude = DefaultCityConfig.COORDINATES.latitude,
                longitude = DefaultCityConfig.COORDINATES.longitude,
                cityName = DefaultCityConfig.NAME
            )
        } returns Result.success(mockWeeklyForecast)

        val result = useCase(abTests)

        assertTrue(result.isSuccess)
        val forecastResult = result.getOrNull()
        assertTrue(forecastResult is WeeklyForecastResult.Success)
        assertEquals(mockWeeklyForecast, (forecastResult as WeeklyForecastResult.Success).forecast)

        coVerify(exactly = 0) { locationTracker.getCurrentLocation() }
    }

    @Test
    fun `invoke with give_coords active returns RequiresCoordsInput`() = runTest {
        val abTests = listOf(
            ABTestFlag("put_default_city", false),
            ABTestFlag("give_coords", true),
            ABTestFlag("select_city", false)
        )

        val result = useCase(abTests)

        assertTrue(result.isSuccess)
        assertEquals(WeeklyForecastResult.RequiresCoordsInput, result.getOrNull())
    }

    @Test
    fun `invoke with select_city active returns RequiresCityInput`() = runTest {
        val abTests = listOf(
            ABTestFlag("put_default_city", false),
            ABTestFlag("give_coords", false),
            ABTestFlag("select_city", true)
        )

        val result = useCase(abTests)

        assertTrue(result.isSuccess)
        assertEquals(WeeklyForecastResult.RequiresCityInput, result.getOrNull())
    }

    @Test
    fun `invoke with all flags false uses DeviceGpsStrategy and refreshes forecast for GPS coordinates`() = runTest {
        val abTests = listOf(
            ABTestFlag("put_default_city", false),
            ABTestFlag("give_coords", false),
            ABTestFlag("select_city", false)
        )
        val gpsCoords = Coordinates(37.7749, -122.4194)

        coEvery { locationTracker.getCurrentLocation() } returns gpsCoords
        coEvery {
            weatherRepository.refreshForecast(
                latitude = gpsCoords.latitude,
                longitude = gpsCoords.longitude,
                cityName = null
            )
        } returns Result.success(mockWeeklyForecast)

        val result = useCase(abTests)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { locationTracker.getCurrentLocation() }
        coVerify(exactly = 1) {
            weatherRepository.refreshForecast(gpsCoords.latitude, gpsCoords.longitude, null)
        }
    }
}
