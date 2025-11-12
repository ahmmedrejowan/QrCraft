package com.rejown.qrcraft.presentation.scanner.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.ContentType
import com.rejown.qrcraft.domain.models.ScanResult
import com.rejown.qrcraft.domain.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class ScanHistoryDetailState(
    val scanResult: ScanResult? = null,
    val scanId: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteDialog: Boolean = false
)

class ScanHistoryDetailViewModel(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScanHistoryDetailState())
    val state: StateFlow<ScanHistoryDetailState> = _state.asStateFlow()

    fun loadScan(scanId: Long) {
        viewModelScope.launch {
            _state.value = ScanHistoryDetailState(isLoading = true, scanId = scanId)

            try {
                val entity = scanRepository.getHistoryById(scanId)
                if (entity != null) {
                    // Convert entity back to ScanResult
                    val scanResult = ScanResult(
                        rawValue = entity.content,
                        displayValue = entity.content,
                        format = BarcodeFormat.valueOf(entity.format),
                        contentType = ContentType.valueOf(entity.contentType),
                        timestamp = entity.timestamp,
                        metadata = null // Metadata stored as JSON, would need parsing
                    )

                    _state.value = ScanHistoryDetailState(
                        scanResult = scanResult,
                        scanId = scanId,
                        isLoading = false
                    )
                } else {
                    _state.value = ScanHistoryDetailState(
                        isLoading = false,
                        error = "Scan not found"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load scan")
                _state.value = ScanHistoryDetailState(
                    isLoading = false,
                    error = "Failed to load scan: ${e.message}"
                )
            }
        }
    }

    fun showDeleteDialog() {
        _state.value = _state.value.copy(showDeleteDialog = true)
    }

    fun hideDeleteDialog() {
        _state.value = _state.value.copy(showDeleteDialog = false)
    }

    suspend fun deleteScan(): Boolean {
        return try {
            val scanId = _state.value.scanId ?: return false
            val entity = scanRepository.getHistoryById(scanId)
            if (entity != null) {
                scanRepository.deleteScan(entity)
                Timber.d("Scan deleted successfully")
                true
            } else {
                Timber.e("Scan not found for deletion")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete scan")
            false
        }
    }
}
