package com.rejown.qrcraft.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejown.qrcraft.data.local.database.entities.ScanHistoryEntity
import com.rejown.qrcraft.domain.repository.ScanRepository
import com.rejown.qrcraft.presentation.scanner.state.ScannerEvent
import com.rejown.qrcraft.presentation.scanner.state.ScannerScreenState
import com.rejown.qrcraft.presentation.scanner.state.ScanningState
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

    private val _state = MutableStateFlow(
        ScannerScreenState(
            isPreviewActive = true,
            scanningState = ScanningState.Scanning
        )
    )
    val state: StateFlow<ScannerScreenState> = _state.asStateFlow()

    fun onEvent(event: ScannerEvent) {
        Timber.tag("QC ScannerViewMode").d("onEvent - Called with ${event::class.simpleName}")
        when (event) {
            is ScannerEvent.StartScanning -> {
                Timber.tag("QC ScannerViewMode").d("onEvent - Setting state to Scanning")
                _state.value = _state.value.copy(scanningState = ScanningState.Scanning)
                Timber.tag("QC ScannerViewMode").d("onEvent - Scanner started")
            }

            is ScannerEvent.StopScanning -> {
                Timber.tag("QC ScannerViewMode").d("onEvent - Setting state to Idle")
                _state.value = _state.value.copy(scanningState = ScanningState.Idle)
                Timber.tag("QC ScannerViewMode").d("onEvent - Scanner stopped")
            }

            is ScannerEvent.OnBarcodeDetected -> {
                Timber.tag("QC ScannerViewMode").d("onEvent - Barcode detected: ${event.result.displayValue}")
                Timber.tag("QC ScannerViewMode").d("onEvent - Setting state to Success")
                _state.value = _state.value.copy(scanningState = ScanningState.Success(event.result))
            }

            is ScannerEvent.OnResultDismissed -> {
                Timber.tag("QC ScannerViewMode").d("onEvent - Result dismissed, setting state to Scanning")
                _state.value = _state.value.copy(scanningState = ScanningState.Scanning)
            }

            is ScannerEvent.OnSaveToHistory -> {
                val currentState = _state.value.scanningState
                Timber.tag("QC ScannerViewMode").d("onEvent - OnSaveToHistory, current state: ${currentState::class.simpleName}")
                if (currentState is ScanningState.Success) {
                    viewModelScope.launch {
                        saveToHistory(currentState.result)
                    }
                } else {
                    Timber.tag("QC ScannerViewMode").d("onEvent - OnSaveToHistory called but state is not Success")
                }
            }

            is ScannerEvent.OnError -> {
                Timber.tag("QC ScannerViewMode").d("onEvent - Setting state to Error: ${event.message}")
                _state.value = _state.value.copy(scanningState = ScanningState.Error(event.message))
                Timber.tag("QC ScannerViewMode").e("onEvent - Scanner error: ${event.message}")
            }

            // Preview control events
            is ScannerEvent.TogglePreview -> {
                val newPreviewState = !_state.value.isPreviewActive
                Timber.tag("QC ScannerViewMode").d("onEvent - Toggling preview: $newPreviewState")
                _state.value = _state.value.copy(isPreviewActive = newPreviewState)
            }

            is ScannerEvent.StartPreview -> {
                Timber.tag("QC ScannerViewMode").d("onEvent - Starting preview")
                _state.value = _state.value.copy(isPreviewActive = true)
            }

            is ScannerEvent.StopPreview -> {
                Timber.tag("QC ScannerViewMode").d("onEvent - Stopping preview")
                _state.value = _state.value.copy(isPreviewActive = false)
            }
        }
    }

    fun getCurrentScanResult(): com.rejown.qrcraft.domain.models.ScanResult? {
        return (_state.value.scanningState as? ScanningState.Success)?.result
    }

    suspend fun saveToHistory(result: com.rejown.qrcraft.domain.models.ScanResult): Long {
        return try {
            Timber.tag("QC ScannerViewMode").d("saveToHistory - Starting save operation for: ${result.displayValue}")

            // Check for duplicate based on format and content
            val existingEntity = scanRepository.findDuplicate(
                format = result.format.name,
                content = result.displayValue
            )

            if (existingEntity != null) {
                // Duplicate found - update timestamp and other fields
                Timber.tag("QC ScannerViewMode").d("saveToHistory - Duplicate found with ID: ${existingEntity.id}, updating timestamp")
                val updatedEntity = existingEntity.copy(
                    timestamp = result.timestamp,
                    rawValue = result.rawValue,
                    metadata = result.metadata?.let { Json.encodeToString(it) }
                )
                scanRepository.updateScan(updatedEntity)
                Timber.tag("QC ScannerViewMode").d("saveToHistory - Duplicate updated successfully")
                existingEntity.id
            } else {
                // No duplicate - insert new entry
                Timber.tag("QC ScannerViewMode").d("saveToHistory - No duplicate found, creating new entry")
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

                val insertedId = scanRepository.insertScan(entity)
                Timber.tag("QC ScannerViewMode").d("saveToHistory - Scan saved to history with ID: $insertedId")
                insertedId
            }
        } catch (e: Exception) {
            Timber.tag("QC ScannerViewMode").e(e, "saveToHistory - Failed to save scan to history")
            -1L // Return -1 on error
        }
    }
}
