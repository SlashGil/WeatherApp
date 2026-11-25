package com.slashgil.weather.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slashgil.weather.core.util.DateFormatter
import com.slashgil.weather.presentation.main.components.CityInputModal
import com.slashgil.weather.presentation.main.components.CoordsInputModal
import com.slashgil.weather.presentation.main.components.EmptyStateView
import com.slashgil.weather.presentation.main.components.ForecastDayCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onRefresh: () -> Unit,
    onEnableDeveloperMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onSubmitCoords: (lat: String, lon: String) -> Unit,
    onSubmitCityQuery: (query: String) -> Unit,
    onDismissCoordsModal: () -> Unit,
    onDismissCityModal: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null && uiState.forecast != null) {
            snackbarHostState.showSnackbar(message)
            onDismissError()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("7-Day Weather") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Forecast"
                        )
                    }
                    if (uiState.isDeveloperModeEnabled) {
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Developer Settings"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && uiState.forecast == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.forecast != null -> {
                    val forecast = uiState.forecast
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = forecast.cityName ?: "Current Location",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = DateFormatter.formatLastUpdatedTimestamp(forecast.lastUpdatedTimestamp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(forecast.dailyForecasts) { daily ->
                            ForecastDayCard(dailyForecast = daily)
                        }
                    }
                }

                else -> {
                    EmptyStateView(
                        onDoubleTap = onEnableDeveloperMode
                    )
                }
            }

            if (uiState.showCoordsModal) {
                CoordsInputModal(
                    errorMessage = uiState.modalErrorMessage,
                    onDismiss = onDismissCoordsModal,
                    onSubmit = onSubmitCoords
                )
            }

            if (uiState.showCityModal) {
                CityInputModal(
                    isLoading = uiState.isModalLoading,
                    errorMessage = uiState.modalErrorMessage,
                    onDismiss = onDismissCityModal,
                    onSubmit = onSubmitCityQuery
                )
            }
        }
    }
}
