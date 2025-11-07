package com.rejown.qrcraft.presentation.scanner.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rejown.qrcraft.domain.models.ContentType
import com.rejown.qrcraft.domain.models.ScanResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultBottomSheet(
    scanResult: ScanResult,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Content type badge
            Text(
                text = scanResult.contentType.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scanned content
            Text(
                text = scanResult.displayValue,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Format info
            Text(
                text = scanResult.format.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy")
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary action based on content type
            when (scanResult.contentType) {
                ContentType.URL -> {
                    FilledTonalButton(
                        onClick = onOpen,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open in Browser")
                    }
                }
                else -> {
                    FilledTonalButton(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save to History")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
