package com.rejown.qrcraft.presentation.history.state

import com.rejown.qrcraft.domain.models.GeneratedCodeData
import com.rejown.qrcraft.domain.models.ScanHistory

data class HistoryState(
    val selectedTab: HistoryTab = HistoryTab.ALL,
    val scannedHistory: List<ScanHistory> = emptyList(),
    val generatedHistory: List<GeneratedCodeData> = emptyList(),
    val combinedHistory: List<HistoryItemData> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class HistoryTab {
    ALL,
    SCANNED,
    GENERATED
}

enum class HistoryItemType {
    SCANNED,
    GENERATED
}

sealed class HistoryItemData(
    open val id: Long,
    open val timestamp: Long,
    open val type: HistoryItemType
) {
    data class Scanned(
        override val id: Long,
        override val timestamp: Long,
        val data: ScanHistory
    ) : HistoryItemData(id, timestamp, HistoryItemType.SCANNED)

    data class Generated(
        override val id: Long,
        override val timestamp: Long,
        val data: GeneratedCodeData
    ) : HistoryItemData(id, timestamp, HistoryItemType.GENERATED)
}

sealed interface HistoryEvent {
    data class OnTabSelected(val tab: HistoryTab) : HistoryEvent
    data class OnSearchQueryChanged(val query: String) : HistoryEvent
    data class OnFilterSelected(val filter: String?) : HistoryEvent
    data class OnToggleFavorite(val id: Long, val isFavorite: Boolean) : HistoryEvent
}
