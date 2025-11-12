package com.rejown.qrcraft.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejown.qrcraft.data.local.preferences.ThemePreferences
import com.rejown.qrcraft.domain.repository.GeneratorRepository
import com.rejown.qrcraft.domain.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val scanRepository: ScanRepository,
    private val generatorRepository: GeneratorRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val scanCount = scanRepository.getCount()
                val generatedCount = generatorRepository.getCount()
                _state.value = _state.value.copy(
                    scanCount = scanCount,
                    generatedCount = generatedCount,
                    totalCount = scanCount + generatedCount
                )
            } catch (e: Exception) {
                // Silently fail for stats
            }
        }
    }

    fun clearScanHistory() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                scanRepository.deleteAll()
                loadStats() // Refresh stats
                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Scan history cleared"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to clear scan history: ${e.message}"
                )
            }
        }
    }

    fun clearGeneratedHistory() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                generatorRepository.deleteAll()
                loadStats() // Refresh stats
                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Generated codes cleared"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to clear generated history: ${e.message}"
                )
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                scanRepository.deleteAll()
                generatorRepository.deleteAll()
                // Reset preferences to defaults
                themePreferences.setTheme("System")
                themePreferences.setDynamicColor(false)
                loadStats() // Refresh stats
                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "All data cleared"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to clear all data: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(successMessage = null, error = null)
    }
}

data class SettingsState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
    val scanCount: Int = 0,
    val generatedCount: Int = 0,
    val totalCount: Int = 0
)
