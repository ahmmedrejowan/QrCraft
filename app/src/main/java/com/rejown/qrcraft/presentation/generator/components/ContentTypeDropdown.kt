package com.rejown.qrcraft.presentation.generator.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rejown.qrcraft.domain.models.ContentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentTypeDropdown(
    selectedType: ContentType,
    onTypeSelected: (ContentType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Content Type",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select content type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                ContentType.values().forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = getContentTypeDescription(type),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun getContentTypeDescription(type: ContentType): String {
    return when (type) {
        ContentType.URL -> "Website link or web address"
        ContentType.EMAIL -> "Email address"
        ContentType.PHONE -> "Phone number"
        ContentType.SMS -> "Text message"
        ContentType.WIFI -> "WiFi credentials"
        ContentType.CONTACT -> "Contact information (vCard)"
        ContentType.CALENDAR -> "Calendar event"
        ContentType.GEO -> "Geographic location"
        ContentType.TEXT -> "Plain text content"
        ContentType.CRYPTO -> "Cryptocurrency address"
        ContentType.PRODUCT -> "Product barcode (EAN/UPC)"
    }
}
