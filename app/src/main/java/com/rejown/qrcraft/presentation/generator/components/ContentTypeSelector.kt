package com.rejown.qrcraft.presentation.generator.components

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
import com.rejown.qrcraft.domain.models.ContentType

@Composable
fun ContentTypeSelector(
    selectedType: ContentType,
    onTypeSelected: (ContentType) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentTypes = listOf(
        ContentType.TEXT,
        ContentType.URL,
        ContentType.EMAIL,
        ContentType.PHONE,
        ContentType.SMS,
        ContentType.WIFI,
        ContentType.CONTACT,
        ContentType.GEO
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        contentTypes.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.displayName) }
            )
        }
    }
}
