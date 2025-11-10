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

    private val _state = MutableStateFlow<ScannerState>(ScannerState.Scanning)
    val state: StateFlow<ScannerState> = _state.asStateFlow()

    fun onEvent(event: ScannerEvent) {
        Timber.tag("QRCraft ScannerViewMode").e("onEvent - Called with ${event::class.simpleName}")
        when (event) {
            is ScannerEvent.StartScanning -> {
                Timber.tag("QRCraft ScannerViewMode").e("onEvent - Setting state to Scanning (was: ${_state.value::class.simpleName})")
                _state.value = ScannerState.Scanning
                Timber.tag("QRCraft ScannerViewMode").e("onEvent - Scanner started")
            }

            is ScannerEvent.StopScanning -> {
                Timber.tag("QRCraft ScannerViewMode").e("onEvent - Setting state to Idle (was: ${_state.value::class.simpleName})")
                _state.value = ScannerState.Idle
                Timber.tag("QRCraft ScannerViewMode").e("onEvent - Scanner stopped")
            }

            is ScannerEvent.OnBarcodeDetected -> {
                Timber.tag("QRCraft ScannerViewMode").e("onEvent - Barcode detected: ${event.result.displayValue}")
                Timber.tag("QRCraft ScannerViewMode").e("onEvent - Setting state to Success")
                _state.value = ScannerState.Success(event.result)
            }

            is ScannerEvent.OnResultDismissed -> {
                Timber.tag("QRCraft ScannerViewMode").e("onEvent - Result dismissed, setting state to Scanning (was: ${_state.value::class.simpleName})")
                _state.value = ScannerState.Scanning
            }

            is ScannerEvent.OnSaveToHistory -> {
                val currentState = _state.value
                Timber.tag("QRCraft ScannerViewMode").e("onEvent - OnSaveToHistory, current state: ${currentState::class.simpleName}")
                if (currentState is ScannerState.Success) {
                    viewModelScope.launch {
                        saveToHistory(currentState.result)
                    }
                } else {
                    Timber.tag("QRCraft ScannerViewMode").e("onEvent - OnSaveToHistory called but state is not Success")
                }
            }

            is ScannerEvent.OnError -> {
                Timber.tag("QRCraft ScannerViewMode").e("onEvent - Setting state to Error (was: ${_state.value::class.simpleName})")
                _state.value = ScannerState.Error(event.message)
                Timber.tag("QRCraft ScannerViewMode").e("onEvent - Scanner error: ${event.message}")
            }
        }
    }

    fun getCurrentScanResult(): com.rejown.qrcraft.domain.models.ScanResult? {
        return (_state.value as? ScannerState.Success)?.result
    }

    suspend fun saveToHistory(result: com.rejown.qrcraft.domain.models.ScanResult): Long {
        return try {
            Timber.tag("QRCraft ScannerViewMode").e("saveToHistory - Starting save operation for: ${result.displayValue}")
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

            Timber.tag("QRCraft ScannerViewMode").e("saveToHistory - Entity created, calling repository.insertScan")
            val insertedId = scanRepository.insertScan(entity)
            Timber.tag("QRCraft ScannerViewMode").e("saveToHistory - Scan saved to history with ID: $insertedId")
            insertedId
        } catch (e: Exception) {
            Timber.tag("QRCraft ScannerViewMode").e(e, "saveToHistory - Failed to save scan to history")
            -1L // Return -1 on error
        }
    }
}
