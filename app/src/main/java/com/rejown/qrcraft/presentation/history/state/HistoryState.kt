package com.rejown.qrcraft.presentation.history.state

import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity
import com.rejown.qrcraft.data.local.database.entities.ScanHistoryEntity

data class HistoryState(
    val selectedTab: HistoryTab = HistoryTab.SCANNED,
    val scannedHistory: List<ScanHistoryEntity> = emptyList(),
    val generatedHistory: List<GeneratedCodeEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedItems: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false
)

enum class HistoryTab {
    SCANNED,
    GENERATED
}

sealed interface HistoryEvent {
    data class OnTabSelected(val tab: HistoryTab) : HistoryEvent
    data class OnSearchQueryChanged(val query: String) : HistoryEvent
    data class OnFilterSelected(val filter: String?) : HistoryEvent
    data class OnItemClicked(val id: Long) : HistoryEvent
    data class OnItemLongPressed(val id: Long) : HistoryEvent
    data class OnToggleFavorite(val id: Long, val isFavorite: Boolean) : HistoryEvent
    data class OnDeleteItem(val id: Long) : HistoryEvent
    data object OnDeleteSelected : HistoryEvent
    data object OnClearSelection : HistoryEvent
    data object OnDeleteAll : HistoryEvent
}
