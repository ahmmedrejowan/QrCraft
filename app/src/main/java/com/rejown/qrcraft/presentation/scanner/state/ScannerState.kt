package com.rejown.qrcraft.presentation.scanner.state

import com.rejown.qrcraft.domain.models.ScanResult

sealed interface ScannerState {
    data object Idle : ScannerState
    data object Scanning : ScannerState
    data class Success(val result: ScanResult) : ScannerState
    data class Error(val message: String) : ScannerState
}

sealed interface ScannerEvent {
    data object StartScanning : ScannerEvent
    data object StopScanning : ScannerEvent
    data class OnBarcodeDetected(val result: ScanResult) : ScannerEvent
    data object OnResultDismissed : ScannerEvent
    data object OnSaveToHistory : ScannerEvent
    data class OnError(val message: String) : ScannerEvent
}
