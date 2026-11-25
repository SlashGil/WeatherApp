package com.slashgil.weather

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slashgil.weather.presentation.main.WeatherScreen
import com.slashgil.weather.presentation.main.WeatherViewModel
import com.slashgil.weather.presentation.settings.SettingsActivity
import com.slashgil.weather.ui.theme.WeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.refreshForecast()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        setContent {
            WeatherAppTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.refreshForecast()
                }

                WeatherScreen(
                    uiState = uiState,
                    onRefresh = { viewModel.refreshForecast() },
                    onEnableDeveloperMode = { viewModel.enableDeveloperMode() },
                    onOpenSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onSubmitCoords = { lat, lon ->
                        viewModel.submitCoordinates(lat, lon)
                    },
                    onSubmitCityQuery = { query ->
                        viewModel.submitCityQuery(query)
                    },
                    onDismissCoordsModal = { viewModel.dismissCoordsModal() },
                    onDismissCityModal = { viewModel.dismissCityModal() },
                    onDismissError = { viewModel.dismissErrorMessage() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshForecast()
    }
}
