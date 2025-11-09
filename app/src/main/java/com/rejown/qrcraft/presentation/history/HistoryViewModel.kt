package com.rejown.qrcraft.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejown.qrcraft.domain.repository.GeneratorRepository
import com.rejown.qrcraft.domain.repository.ScanRepository
import com.rejown.qrcraft.presentation.history.state.HistoryEvent
import com.rejown.qrcraft.presentation.history.state.HistoryItemData
import com.rejown.qrcraft.presentation.history.state.HistoryState
import com.rejown.qrcraft.presentation.history.state.HistoryTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class HistoryViewModel(
    private val scanRepository: ScanRepository,
    private val generatorRepository: GeneratorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        loadHistory()
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.OnTabSelected -> {
                _state.update {
                    it.copy(
                        selectedTab = event.tab,
                        searchQuery = "",
                        selectedFilter = null,
                        selectedItems = emptySet(),
                        isSelectionMode = false
                    )
                }
            }

            is HistoryEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                searchHistory(event.query)
            }

            is HistoryEvent.OnFilterSelected -> {
                _state.update { it.copy(selectedFilter = event.filter) }
                applyFilter(event.filter)
            }

            is HistoryEvent.OnItemClicked -> {
                if (_state.value.isSelectionMode) {
                    toggleItemSelection(event.id)
                }
                // Navigation handled in UI
            }

            is HistoryEvent.OnItemLongPressed -> {
                toggleItemSelection(event.id)
                if (!_state.value.isSelectionMode) {
                    _state.update { it.copy(isSelectionMode = true) }
                }
            }

            is HistoryEvent.OnToggleFavorite -> {
                toggleFavorite(event.id, event.isFavorite)
            }

            is HistoryEvent.OnDeleteItem -> {
                deleteItem(event.id)
            }

            is HistoryEvent.OnDeleteSelected -> {
                deleteSelectedItems()
            }

            is HistoryEvent.OnClearSelection -> {
                _state.update {
                    it.copy(
                        selectedItems = emptySet(),
                        isSelectionMode = false
                    )
                }
            }

            is HistoryEvent.OnDeleteAll -> {
                deleteAllHistory()
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Combine both flows to update state when either changes
                combine(
                    scanRepository.getAllHistory(),
                    generatorRepository.getAllGenerated()
                ) { scannedList, generatedList ->
                    Pair(scannedList, generatedList)
                }
                    .catch { e ->
                        Timber.e(e, "Error loading history")
                        _state.update { it.copy(error = "Failed to load history", isLoading = false) }
                    }
                    .collect { (scannedList, generatedList) ->
                        // Create combined list sorted by timestamp
                        val combined = buildList {
                            scannedList.forEach { scan ->
                                add(
                                    HistoryItemData.Scanned(
                                        id = scan.id,
                                        timestamp = scan.timestamp,
                                        entity = scan
                                    )
                                )
                            }
                            generatedList.forEach { gen ->
                                add(
                                    HistoryItemData.Generated(
                                        id = gen.id,
                                        timestamp = gen.createdAt,
                                        entity = gen
                                    )
                                )
                            }
                        }.sortedByDescending { it.timestamp }

                        _state.update {
                            it.copy(
                                scannedHistory = scannedList,
                                generatedHistory = generatedList,
                                combinedHistory = combined,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error in loadHistory")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun searchHistory(query: String) {
        if (query.isEmpty()) {
            loadHistory()
            return
        }

        viewModelScope.launch {
            try {
                when (_state.value.selectedTab) {
                    HistoryTab.ALL -> {
                        // Search both and combine
                        combine(
                            scanRepository.searchHistory(query),
                            generatorRepository.searchGenerated(query)
                        ) { scannedResults, generatedResults ->
                            Pair(scannedResults, generatedResults)
                        }.collect { (scannedResults, generatedResults) ->
                            val combined = buildList {
                                scannedResults.forEach { scan ->
                                    add(HistoryItemData.Scanned(scan.id, scan.timestamp, scan))
                                }
                                generatedResults.forEach { gen ->
                                    add(HistoryItemData.Generated(gen.id, gen.createdAt, gen))
                                }
                            }.sortedByDescending { it.timestamp }

                            _state.update {
                                it.copy(
                                    scannedHistory = scannedResults,
                                    generatedHistory = generatedResults,
                                    combinedHistory = combined
                                )
                            }
                        }
                    }
                    HistoryTab.SCANNED -> {
                        scanRepository.searchHistory(query).collect { results ->
                            _state.update { it.copy(scannedHistory = results) }
                        }
                    }
                    HistoryTab.GENERATED -> {
                        generatorRepository.searchGenerated(query).collect { results ->
                            _state.update { it.copy(generatedHistory = results) }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error searching history")
            }
        }
    }

    private fun applyFilter(filter: String?) {
        if (filter == null) {
            loadHistory()
            return
        }

        viewModelScope.launch {
            try {
                when (_state.value.selectedTab) {
                    HistoryTab.ALL -> {
                        // Filter both and combine
                        combine(
                            scanRepository.getHistoryByType(filter),
                            generatorRepository.getGeneratedByType(filter)
                        ) { scannedResults, generatedResults ->
                            Pair(scannedResults, generatedResults)
                        }.collect { (scannedResults, generatedResults) ->
                            val combined = buildList {
                                scannedResults.forEach { scan ->
                                    add(HistoryItemData.Scanned(scan.id, scan.timestamp, scan))
                                }
                                generatedResults.forEach { gen ->
                                    add(HistoryItemData.Generated(gen.id, gen.createdAt, gen))
                                }
                            }.sortedByDescending { it.timestamp }

                            _state.update {
                                it.copy(
                                    scannedHistory = scannedResults,
                                    generatedHistory = generatedResults,
                                    combinedHistory = combined
                                )
                            }
                        }
                    }
                    HistoryTab.SCANNED -> {
                        scanRepository.getHistoryByType(filter).collect { results ->
                            _state.update { it.copy(scannedHistory = results) }
                        }
                    }
                    HistoryTab.GENERATED -> {
                        generatorRepository.getGeneratedByType(filter).collect { results ->
                            _state.update { it.copy(generatedHistory = results) }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error filtering history")
            }
        }
    }

    private fun toggleItemSelection(id: Long) {
        _state.update {
            val newSelection = if (it.selectedItems.contains(id)) {
                it.selectedItems - id
            } else {
                it.selectedItems + id
            }
            it.copy(
                selectedItems = newSelection,
                isSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    private fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                // For ALL tab, we need to check which type of item it is
                if (_state.value.selectedTab == HistoryTab.ALL) {
                    // Check if it's in scanned history first
                    val scannedItem = scanRepository.getHistoryById(id)
                    if (scannedItem != null) {
                        scanRepository.updateScan(scannedItem.copy(isFavorite = isFavorite))
                    } else {
                        // Must be in generated history
                        val generatedItem = generatorRepository.getGeneratedById(id)
                        generatedItem?.let {
                            generatorRepository.updateGenerated(it.copy(isFavorite = isFavorite))
                        }
                    }
                } else if (_state.value.selectedTab == HistoryTab.SCANNED) {
                    val item = scanRepository.getHistoryById(id)
                    item?.let {
                        scanRepository.updateScan(it.copy(isFavorite = isFavorite))
                    }
                } else {
                    val item = generatorRepository.getGeneratedById(id)
                    item?.let {
                        generatorRepository.updateGenerated(it.copy(isFavorite = isFavorite))
                    }
                }
                Timber.d("Toggled favorite for item $id")
            } catch (e: Exception) {
                Timber.e(e, "Error toggling favorite")
            }
        }
    }

    private fun deleteItem(id: Long) {
        viewModelScope.launch {
            try {
                // For ALL tab, we need to check which type of item it is
                if (_state.value.selectedTab == HistoryTab.ALL) {
                    // Check if it's in scanned history first
                    val scannedItem = scanRepository.getHistoryById(id)
                    if (scannedItem != null) {
                        scanRepository.deleteScan(scannedItem)
                    } else {
                        // Must be in generated history
                        val generatedItem = generatorRepository.getGeneratedById(id)
                        generatedItem?.let { generatorRepository.deleteGenerated(it) }
                    }
                } else if (_state.value.selectedTab == HistoryTab.SCANNED) {
                    val item = scanRepository.getHistoryById(id)
                    item?.let { scanRepository.deleteScan(it) }
                } else {
                    val item = generatorRepository.getGeneratedById(id)
                    item?.let { generatorRepository.deleteGenerated(it) }
                }
                Timber.d("Deleted item $id")
            } catch (e: Exception) {
                Timber.e(e, "Error deleting item")
            }
        }
    }

    private fun deleteSelectedItems() {
        viewModelScope.launch {
            try {
                val ids = _state.value.selectedItems.toList()

                if (_state.value.selectedTab == HistoryTab.ALL) {
                    // For ALL tab, separate IDs by type
                    val scannedIds = mutableListOf<Long>()
                    val generatedIds = mutableListOf<Long>()

                    ids.forEach { id ->
                        // Check which repository contains this ID
                        val scannedItem = scanRepository.getHistoryById(id)
                        if (scannedItem != null) {
                            scannedIds.add(id)
                        } else {
                            generatedIds.add(id)
                        }
                    }

                    if (scannedIds.isNotEmpty()) {
                        scanRepository.deleteByIds(scannedIds)
                    }
                    if (generatedIds.isNotEmpty()) {
                        generatorRepository.deleteByIds(generatedIds)
                    }
                } else if (_state.value.selectedTab == HistoryTab.SCANNED) {
                    scanRepository.deleteByIds(ids)
                } else {
                    generatorRepository.deleteByIds(ids)
                }

                _state.update {
                    it.copy(
                        selectedItems = emptySet(),
                        isSelectionMode = false
                    )
                }
                Timber.d("Deleted ${ids.size} items")
            } catch (e: Exception) {
                Timber.e(e, "Error deleting selected items")
            }
        }
    }

    private fun deleteAllHistory() {
        viewModelScope.launch {
            try {
                if (_state.value.selectedTab == HistoryTab.ALL) {
                    // Delete both
                    scanRepository.deleteAll()
                    generatorRepository.deleteAll()
                } else if (_state.value.selectedTab == HistoryTab.SCANNED) {
                    scanRepository.deleteAll()
                } else {
                    generatorRepository.deleteAll()
                }
                Timber.d("Deleted all history")
            } catch (e: Exception) {
                Timber.e(e, "Error deleting all history")
            }
        }
    }
}
