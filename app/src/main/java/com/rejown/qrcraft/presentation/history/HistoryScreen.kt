package com.rejown.qrcraft.presentation.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rejown.qrcraft.presentation.history.components.FilterChips
import com.rejown.qrcraft.presentation.history.components.HistoryItem
import com.rejown.qrcraft.presentation.history.components.SearchBar
import com.rejown.qrcraft.presentation.history.state.HistoryEvent
import com.rejown.qrcraft.presentation.history.state.HistoryItemData
import com.rejown.qrcraft.presentation.history.state.HistoryTab
import com.rejown.qrcraft.presentation.navigation.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title
            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            // Tabs (Reordered: All, Generated, Scanned)
            TabRow(
                selectedTabIndex = when (state.selectedTab) {
                    HistoryTab.ALL -> 0
                    HistoryTab.GENERATED -> 1
                    HistoryTab.SCANNED -> 2
                }
            ) {
                Tab(
                    selected = state.selectedTab == HistoryTab.ALL,
                    onClick = {
                        viewModel.onEvent(HistoryEvent.OnTabSelected(HistoryTab.ALL))
                    },
                    text = { Text("All") },
                    modifier = Modifier.height(48.dp)
                )
                Tab(
                    selected = state.selectedTab == HistoryTab.GENERATED,
                    onClick = {
                        viewModel.onEvent(HistoryEvent.OnTabSelected(HistoryTab.GENERATED))
                    },
                    text = { Text("Generated") },
                    modifier = Modifier.height(48.dp)
                )
                Tab(
                    selected = state.selectedTab == HistoryTab.SCANNED,
                    onClick = {
                        viewModel.onEvent(HistoryEvent.OnTabSelected(HistoryTab.SCANNED))
                    },
                    text = { Text("Scanned") },
                    modifier = Modifier.height(48.dp)
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
                                key = { "${it.type.name}_${it.id}" }
                            ) { item ->
                                when (item) {
                                    is HistoryItemData.Scanned -> {
                                        HistoryItem(
                                            title = null,
                                            content = item.entity.content,
                                            contentType = item.entity.contentType,
                                            format = item.entity.format,
                                            timestamp = item.entity.timestamp,
                                            isFavorite = item.entity.isFavorite,
                                            tag = "Scanned",
                                            onClicked = {
                                                navController.navigate(
                                                    Screen.ScanHistoryDetail(scanId = item.id)
                                                )
                                            },
                                            onToggleFavorite = {
                                                viewModel.onEvent(
                                                    HistoryEvent.OnToggleFavorite(
                                                        item.id,
                                                        !item.entity.isFavorite
                                                    )
                                                )
                                            },
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                    is HistoryItemData.Generated -> {
                                        HistoryItem(
                                            title = item.entity.title,
                                            content = item.entity.formattedContent,
                                            contentType = item.entity.templateName,
                                            format = item.entity.barcodeFormat,
                                            timestamp = item.entity.createdAt,
                                            isFavorite = item.entity.isFavorite,
                                            tag = "Generated",
                                            onClicked = {
                                                navController.navigate(
                                                    Screen.CodeDetails(codeId = item.id)
                                                )
                                            },
                                            onToggleFavorite = {
                                                viewModel.onEvent(
                                                    HistoryEvent.OnToggleFavorite(
                                                        item.id,
                                                        !item.entity.isFavorite
                                                    )
                                                )
                                            },
                                            modifier = Modifier.padding(bottom = 12.dp)
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
                                    title = null,
                                    content = item.content,
                                    contentType = item.contentType,
                                    format = item.format,
                                    timestamp = item.timestamp,
                                    isFavorite = item.isFavorite,
                                    onClicked = {
                                        navController.navigate(
                                            Screen.ScanHistoryDetail(scanId = item.id)
                                        )
                                    },
                                    onToggleFavorite = {
                                        viewModel.onEvent(
                                            HistoryEvent.OnToggleFavorite(
                                                item.id,
                                                !item.isFavorite
                                            )
                                        )
                                    },
                                    modifier = Modifier.padding(bottom = 12.dp)
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
                                    title = item.title,
                                    content = item.formattedContent,
                                    contentType = item.templateName,
                                    format = item.barcodeFormat,
                                    timestamp = item.createdAt,
                                    isFavorite = item.isFavorite,
                                    onClicked = {
                                        navController.navigate(
                                            Screen.CodeDetails(codeId = item.id)
                                        )
                                    },
                                    onToggleFavorite = {
                                        viewModel.onEvent(
                                            HistoryEvent.OnToggleFavorite(
                                                item.id,
                                                !item.isFavorite
                                            )
                                        )
                                    },
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                        }
                    }
                }
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
