package com.slashgil.weather.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slashgil.weather.domain.model.ABTestFlag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    abTests: List<ABTestFlag>,
    onToggleFlag: (name: String, newValue: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val putDefaultCityValue = abTests.find { it.name == "put_default_city" }?.value == true
    val giveCoordsValue = abTests.find { it.name == "give_coords" }?.value == true
    val selectCityValue = abTests.find { it.name == "select_city" }?.value == true

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Developer Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "A/B Test Configurations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Only one A/B test flag can be active at a time. Toggling all off restores standard GPS location.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            ABTestItemCard(
                title = "put_default_city",
                subtitle = "Skips GPS and retrieves forecast for default city (New York, NY)",
                checked = putDefaultCityValue,
                onCheckedChange = { onToggleFlag("put_default_city", it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ABTestItemCard(
                title = "give_coords",
                subtitle = "Prompts a modal dialog to enter custom Latitude and Longitude",
                checked = giveCoordsValue,
                onCheckedChange = { onToggleFlag("give_coords", it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ABTestItemCard(
                title = "select_city",
                subtitle = "Prompts a modal dialog to enter city name (city, state, country)",
                checked = selectCityValue,
                onCheckedChange = { onToggleFlag("select_city", it) }
            )
        }
    }
}

@Composable
private fun ABTestItemCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
