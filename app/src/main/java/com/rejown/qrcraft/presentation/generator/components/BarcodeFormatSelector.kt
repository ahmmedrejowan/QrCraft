package com.rejown.qrcraft.presentation.generator.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.BarcodeType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BarcodeFormatSelector(
    selectedType: BarcodeType,
    selectedFormat: BarcodeFormat,
    onTypeSelected: (BarcodeType) -> Unit,
    onFormatSelected: (BarcodeFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = listOf(BarcodeType.TWO_D, BarcodeType.ONE_D)
    val selectedIndex = types.indexOf(selectedType)

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Barcode Type",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Tabs for 1D/2D
        PrimaryTabRow(
            selectedTabIndex = selectedIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            types.forEachIndexed { index, type ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onTypeSelected(type) },
                    text = { Text(type.displayName) }
                )
            }
        }

        // Format chips
        Text(
            text = "Format",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            BarcodeFormat.getByType(selectedType).forEach { format ->
                FilterChip(
                    selected = selectedFormat == format,
                    onClick = { onFormatSelected(format) },
                    label = { Text(format.displayName) }
                )
            }
        }
    }
}
