package com.slashgil.weather.data.repository

import com.slashgil.weather.data.local.dao.ForecastDao
import com.slashgil.weather.data.local.entity.DailyForecastEntity
import com.slashgil.weather.data.local.entity.ForecastEntity
import com.slashgil.weather.data.local.entity.ForecastWithDailyEntity
import com.slashgil.weather.data.remote.api.OpenMeteoForecastApi
import com.slashgil.weather.data.remote.api.OpenMeteoGeocodingApi
import com.slashgil.weather.data.remote.dto.DailyDto
import com.slashgil.weather.data.remote.dto.ForecastResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherRepositoryImplTest {

    private val forecastApi: OpenMeteoForecastApi = mockk()
    private val geocodingApi: OpenMeteoGeocodingApi = mockk()
    private val forecastDao: ForecastDao = mockk(relaxed = true)
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setUp() {
        repository = WeatherRepositoryImpl(forecastApi, geocodingApi, forecastDao)
    }

    @Test
    fun `observeCachedForecast returns mapped domain model from Room DAO`() = runTest {
        val cachedEntity = ForecastWithDailyEntity(
            forecast = ForecastEntity(
                id = 1,
                cityName = "New York, NY",
                latitude = 40.7128,
                longitude = -74.0060,
                timezone = "America/New_York",
                lastUpdatedTimestamp = 123456789L
            ),
            dailyForecasts = listOf(
                DailyForecastEntity(
                    id = 1,
                    forecastOwnerId = 1,
                    date = "2026-11-24",
                    weatherCode = 0,
                    tempMax = 15.0,
                    tempMin = 5.0,
                    precipitationProbabilityMax = 10
                )
            )
        )

        every { forecastDao.getForecastWithDailyFlow() } returns flowOf(cachedEntity)

        val result = repository.observeCachedForecast().first()

        assertNotNull(result)
        assertEquals("New York, NY", result?.cityName)
        assertEquals(40.7128, result?.latitude ?: 0.0, 0.0001)
        assertEquals(1, result?.dailyForecasts?.size)
    }

    @Test
    fun `refreshForecast on network success saves forecast to Room DAO and returns WeeklyForecast`() = runTest {
        val dto = ForecastResponseDto(
            latitude = 40.7128,
            longitude = -74.0060,
            timezone = "America/New_York",
            daily = DailyDto(
                time = listOf("2026-11-24"),
                weatherCode = listOf(0),
                tempMax = listOf(20.0),
                tempMin = listOf(10.0),
                precipitationProbabilityMax = listOf(0)
            )
        )

        coEvery { forecastApi.getWeeklyForecast(40.7128, -74.0060) } returns dto

        val result = repository.refreshForecast(40.7128, -74.0060, "New York, NY")

        assertTrue(result.isSuccess)
        val weeklyForecast = result.getOrNull()
        assertNotNull(weeklyForecast)
        assertEquals("New York, NY", weeklyForecast?.cityName)
        coVerify(exactly = 1) { forecastDao.saveForecast(any(), any()) }
    }

    @Test
    fun `refreshForecast on network failure returns failure without updating Room DAO`() = runTest {
        coEvery { forecastApi.getWeeklyForecast(any(), any()) } throws RuntimeException("Network error")

        val result = repository.refreshForecast(40.7128, -74.0060)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { forecastDao.saveForecast(any(), any()) }
    }
}
