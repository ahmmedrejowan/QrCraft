package com.rejown.qrcraft.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Confirmation dialog for destructive actions
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    icon: ImageVector = Icons.Default.Warning,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

/**
 * Delete confirmation dialog
 */
@Composable
fun DeleteConfirmationDialog(
    itemName: String = "this item",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        title = "Delete $itemName?",
        message = "This action cannot be undone. Are you sure you want to delete $itemName?",
        confirmText = "Delete",
        dismissText = "Cancel",
        icon = Icons.Default.Delete,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Clear all confirmation dialog
 */
@Composable
fun ClearAllDialog(
    itemType: String = "items",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        title = "Clear all $itemType?",
        message = "This will permanently delete all $itemType. This action cannot be undone.",
        confirmText = "Clear All",
        dismissText = "Cancel",
        icon = Icons.Default.Delete,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Error dialog with details
 */
@Composable
fun ErrorDialog(
    title: String = "Error",
    message: String,
    dismissText: String = "OK",
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

/**
 * Info dialog
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    dismissText: String = "Got it",
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

/**
 * Permission rationale dialog
 */
@Composable
fun PermissionRationaleDialog(
    permissionName: String,
    rationale: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        title = "$permissionName Permission Required",
        message = rationale,
        confirmText = "Grant Permission",
        dismissText = "Not Now",
        icon = Icons.Default.Info,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
