package com.rejown.qrcraft.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejown.qrcraft.data.local.database.entities.ScanHistoryEntity
import com.rejown.qrcraft.domain.repository.ScanRepository
import com.rejown.qrcraft.presentation.scanner.state.ScannerEvent
import com.rejown.qrcraft.presentation.scanner.state.ScannerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

class ScannerViewModel(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val state: StateFlow<ScannerState> = _state.asStateFlow()

    init {
        onEvent(ScannerEvent.StartScanning)
    }

    fun onEvent(event: ScannerEvent) {
        when (event) {
            is ScannerEvent.StartScanning -> {
                _state.value = ScannerState.Scanning
                Timber.d("Scanner started")
            }

            is ScannerEvent.StopScanning -> {
                _state.value = ScannerState.Idle
                Timber.d("Scanner stopped")
            }

            is ScannerEvent.OnBarcodeDetected -> {
                _state.value = ScannerState.Success(event.result)
                Timber.d("Barcode detected: ${event.result.displayValue}")
            }

            is ScannerEvent.OnResultDismissed -> {
                _state.value = ScannerState.Scanning
            }

            is ScannerEvent.OnSaveToHistory -> {
                val currentState = _state.value
                if (currentState is ScannerState.Success) {
                    saveToHistory(currentState.result)
                }
            }

            is ScannerEvent.OnError -> {
                _state.value = ScannerState.Error(event.message)
                Timber.e("Scanner error: ${event.message}")
            }
        }
    }

    private fun saveToHistory(result: com.rejown.qrcraft.domain.models.ScanResult) {
        viewModelScope.launch {
            try {
                val entity = ScanHistoryEntity(
                    content = result.displayValue,
                    rawValue = result.rawValue,
                    format = result.format.name,
                    contentType = result.contentType.name,
                    timestamp = result.timestamp,
                    isFavorite = false,
                    metadata = result.metadata?.let { Json.encodeToString(it) },
                    imagePath = null
                )

                scanRepository.insertScan(entity)
                Timber.d("Scan saved to history")
            } catch (e: Exception) {
                Timber.e(e, "Failed to save scan to history")
            }
        }
    }
}
