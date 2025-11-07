package com.rejown.qrcraft.presentation.history.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterChips(
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        null to "All",
        "URL" to "URLs",
        "EMAIL" to "Emails",
        "PHONE" to "Phone",
        "TEXT" to "Text",
        "WIFI" to "WiFi",
        "CONTACT" to "Contacts"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (filterValue, label) ->
            FilterChip(
                selected = selectedFilter == filterValue,
                onClick = { onFilterSelected(filterValue) },
                label = { Text(label) }
            )
        }
    }
}
