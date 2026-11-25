package com.slashgil.weather.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slashgil.weather.domain.model.ABTestFlag
import com.slashgil.weather.domain.usecase.ObserveABTestsUseCase
import com.slashgil.weather.domain.usecase.ToggleABTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeABTestsUseCase: ObserveABTestsUseCase,
    private val toggleABTestUseCase: ToggleABTestUseCase
) : ViewModel() {

    val abTests: StateFlow<List<ABTestFlag>> = observeABTestsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onToggleFlag(name: String, newValue: Boolean) {
        viewModelScope.launch {
            toggleABTestUseCase(targetName = name, newValue = newValue)
        }
    }
}
