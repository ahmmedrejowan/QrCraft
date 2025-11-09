package com.rejown.qrcraft.presentation.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rejown.qrcraft.presentation.history.components.FilterChips
import com.rejown.qrcraft.presentation.history.components.HistoryItem
import com.rejown.qrcraft.presentation.history.components.SearchBar
import com.rejown.qrcraft.presentation.history.state.HistoryEvent
import com.rejown.qrcraft.presentation.history.state.HistoryItemData
import com.rejown.qrcraft.presentation.history.state.HistoryTab
import com.rejown.qrcraft.presentation.navigation.Screen
import com.rejown.qrcraft.utils.rememberHapticFeedback
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val haptic = rememberHapticFeedback()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = when (state.selectedTab) {
                    HistoryTab.ALL -> 0
                    HistoryTab.SCANNED -> 1
                    HistoryTab.GENERATED -> 2
                }
            ) {
                Tab(
                    selected = state.selectedTab == HistoryTab.ALL,
                    onClick = {
                        viewModel.onEvent(HistoryEvent.OnTabSelected(HistoryTab.ALL))
                        haptic.lightClick()
                    },
                    text = { Text("All") }
                )
                Tab(
                    selected = state.selectedTab == HistoryTab.SCANNED,
                    onClick = {
                        viewModel.onEvent(HistoryEvent.OnTabSelected(HistoryTab.SCANNED))
                        haptic.lightClick()
                    },
                    text = { Text("Scanned") }
                )
                Tab(
                    selected = state.selectedTab == HistoryTab.GENERATED,
                    onClick = {
                        viewModel.onEvent(HistoryEvent.OnTabSelected(HistoryTab.GENERATED))
                        haptic.lightClick()
                    },
                    text = { Text("Generated") }
                )
            }

            // Search bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { query ->
                    viewModel.onEvent(HistoryEvent.OnSearchQueryChanged(query))
                }
            )

            // Filter chips
            FilterChips(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { filter ->
                    viewModel.onEvent(HistoryEvent.OnFilterSelected(filter))
                    haptic.lightClick()
                }
            )

            // History list
            when (state.selectedTab) {
                HistoryTab.ALL -> {
                    if (state.combinedHistory.isEmpty()) {
                        EmptyState("No history yet")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.combinedHistory,
                                key = { it.id }
                            ) { item ->
                                when (item) {
                                    is HistoryItemData.Scanned -> {
                                        HistoryItem(
                                            content = item.entity.content,
                                            contentType = item.entity.contentType,
                                            format = item.entity.format,
                                            timestamp = item.entity.timestamp,
                                            isFavorite = item.entity.isFavorite,
                                            isSelected = state.selectedItems.contains(item.id),
                                            tag = "Scanned",
                                            onClicked = {
                                                if (state.isSelectionMode) {
                                                    viewModel.onEvent(HistoryEvent.OnItemLongPressed(item.id))
                                                } else {
                                                    navController.navigate(
                                                        Screen.ScanHistoryDetail(scanId = item.id)
                                                    )
                                                }
                                            },
                                            onLongPress = {
                                                viewModel.onEvent(HistoryEvent.OnItemLongPressed(item.id))
                                                haptic.strongImpact()
                                            },
                                            onToggleFavorite = {
                                                viewModel.onEvent(
                                                    HistoryEvent.OnToggleFavorite(
                                                        item.id,
                                                        !item.entity.isFavorite
                                                    )
                                                )
                                                haptic.mediumClick()
                                            },
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    is HistoryItemData.Generated -> {
                                        HistoryItem(
                                            content = item.entity.formattedContent,
                                            contentType = item.entity.barcodeType,
                                            format = item.entity.barcodeFormat,
                                            timestamp = item.entity.createdAt,
                                            isFavorite = item.entity.isFavorite,
                                            isSelected = state.selectedItems.contains(item.id),
                                            tag = "Generated",
                                            onClicked = {
                                                if (state.isSelectionMode) {
                                                    viewModel.onEvent(HistoryEvent.OnItemLongPressed(item.id))
                                                } else {
                                                    navController.navigate(
                                                        Screen.Detail(id = item.id, type = "generated")
                                                    )
                                                }
                                            },
                                            onLongPress = {
                                                viewModel.onEvent(HistoryEvent.OnItemLongPressed(item.id))
                                                haptic.strongImpact()
                                            },
                                            onToggleFavorite = {
                                                viewModel.onEvent(
                                                    HistoryEvent.OnToggleFavorite(
                                                        item.id,
                                                        !item.entity.isFavorite
                                                    )
                                                )
                                                haptic.mediumClick()
                                            },
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HistoryTab.SCANNED -> {
                    if (state.scannedHistory.isEmpty()) {
                        EmptyState("No scanned codes yet")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.scannedHistory,
                                key = { it.id }
                            ) { item ->
                                HistoryItem(
                                    content = item.content,
                                    contentType = item.contentType,
                                    format = item.format,
                                    timestamp = item.timestamp,
                                    isFavorite = item.isFavorite,
                                    isSelected = state.selectedItems.contains(item.id),
                                    onClicked = {
                                        if (state.isSelectionMode) {
                                            viewModel.onEvent(HistoryEvent.OnItemLongPressed(item.id))
                                        } else {
                                            navController.navigate(
                                                Screen.ScanHistoryDetail(scanId = item.id)
                                            )
                                        }
                                    },
                                    onLongPress = {
                                        viewModel.onEvent(HistoryEvent.OnItemLongPressed(item.id))
                                        haptic.strongImpact()
                                    },
                                    onToggleFavorite = {
                                        viewModel.onEvent(
                                            HistoryEvent.OnToggleFavorite(
                                                item.id,
                                                !item.isFavorite
                                            )
                                        )
                                        haptic.mediumClick()
                                    },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }

                HistoryTab.GENERATED -> {
                    if (state.generatedHistory.isEmpty()) {
                        EmptyState("No generated codes yet")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.generatedHistory,
                                key = { it.id }
                            ) { item ->
                                HistoryItem(
                                    content = item.formattedContent,
                                    contentType = item.barcodeType,
                                    format = item.barcodeFormat,
                                    timestamp = item.createdAt,
                                    isFavorite = item.isFavorite,
                                    isSelected = state.selectedItems.contains(item.id),
                                    onClicked = {
                                        if (state.isSelectionMode) {
                                            viewModel.onEvent(HistoryEvent.OnItemLongPressed(item.id))
                                        } else {
                                            navController.navigate(
                                                Screen.Detail(id = item.id, type = "generated")
                                            )
                                        }
                                    },
                                    onLongPress = {
                                        viewModel.onEvent(HistoryEvent.OnItemLongPressed(item.id))
                                        haptic.strongImpact()
                                    },
                                    onToggleFavorite = {
                                        viewModel.onEvent(
                                            HistoryEvent.OnToggleFavorite(
                                                item.id,
                                                !item.isFavorite
                                            )
                                        )
                                        haptic.mediumClick()
                                    },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB for bulk delete
        if (state.isSelectionMode && state.selectedItems.isNotEmpty()) {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(HistoryEvent.OnDeleteSelected)
                    haptic.strongImpact()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete selected"
                )
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
