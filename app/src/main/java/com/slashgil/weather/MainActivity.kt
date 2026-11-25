package com.slashgil.weather

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.slashgil.weather.presentation.main.WeatherScreen
import com.slashgil.weather.presentation.main.WeatherViewModel
import com.slashgil.weather.presentation.settings.SettingsActivity
import com.slashgil.weather.ui.theme.WeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.refreshForecast()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var isReady = false
        lifecycleScope.launch {
            viewModel.uiState.first()
            isReady = true
        }
        splashScreen.setKeepOnScreenCondition { !isReady }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            this.splashScreen.setOnExitAnimationListener { platformView ->
                val fadeOut = ObjectAnimator.ofFloat(platformView, View.ALPHA, 1f, 0f)
                AnimatorSet().apply {
                    duration = 350L
                    interpolator = AccelerateDecelerateInterpolator()
                    playTogether(fadeOut)
                    doOnEnd { platformView.remove() }
                    start()
                }
            }
        } else {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                val fadeOut = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f)
                AnimatorSet().apply {
                    duration = 350L
                    interpolator = AccelerateDecelerateInterpolator()
                    playTogether(fadeOut)
                    doOnEnd { splashScreenView.remove() }
                    start()
                }
            }
        }

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
