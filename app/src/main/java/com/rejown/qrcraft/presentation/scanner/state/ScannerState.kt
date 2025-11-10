package com.rejown.qrcraft.presentation.scanner.state

import com.rejown.qrcraft.domain.models.ScanResult

/**
 * Represents the overall scanner screen state
 */
data class ScannerScreenState(
    val isPreviewActive: Boolean = true, // Camera preview on/off
    val scanningState: ScanningState = ScanningState.Scanning
)

/**
 * Represents the scanning operation state
 */
sealed interface ScanningState {
    data object Idle : ScanningState
    data object Scanning : ScanningState
    data class Success(val result: ScanResult) : ScanningState
    data class Error(val message: String) : ScanningState
}

sealed interface ScannerEvent {
    data object StartScanning : ScannerEvent
    data object StopScanning : ScannerEvent
    data class OnBarcodeDetected(val result: ScanResult) : ScannerEvent
    data object OnResultDismissed : ScannerEvent
    data object OnSaveToHistory : ScannerEvent
    data class OnError(val message: String) : ScannerEvent

    // Preview control events
    data object TogglePreview : ScannerEvent
    data object StartPreview : ScannerEvent
    data object StopPreview : ScannerEvent
}
