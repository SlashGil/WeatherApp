package com.slashgil.weather.presentation.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slashgil.weather.ui.theme.WeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                val abTests by viewModel.abTests.collectAsStateWithLifecycle()
                SettingsScreen(
                    abTests = abTests,
                    onToggleFlag = { name, newValue ->
                        viewModel.onToggleFlag(name, newValue)
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}
