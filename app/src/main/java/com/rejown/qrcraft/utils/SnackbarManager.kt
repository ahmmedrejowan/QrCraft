package com.rejown.qrcraft.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manager for displaying snackbars throughout the app
 * Provides a consistent way to show success, error, and info messages
 */
class SnackbarManager(
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope
) {

    /**
     * Show a success message (short duration)
     */
    fun showSuccess(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        show(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Short,
            onAction = onAction
        )
    }

    /**
     * Show an error message (long duration with optional retry)
     */
    fun showError(
        message: String,
        actionLabel: String? = "Retry",
        onAction: (() -> Unit)? = null
    ) {
        show(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long,
            onAction = onAction
        )
        Timber.e("Snackbar error: $message")
    }

    /**
     * Show an info message (short duration)
     */
    fun showInfo(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        show(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Short,
            onAction = onAction
        )
    }

    /**
     * Show a warning message (long duration)
     */
    fun showWarning(
        message: String,
        actionLabel: String? = "OK",
        onAction: (() -> Unit)? = null
    ) {
        show(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long,
            onAction = onAction
        )
    }

    /**
     * Show a message with undo action
     */
    fun showWithUndo(
        message: String,
        onUndo: () -> Unit
    ) {
        show(
            message = message,
            actionLabel = "Undo",
            duration = SnackbarDuration.Long,
            onAction = onUndo
        )
    }

    /**
     * Generic show method
     */
    private fun show(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onAction: (() -> Unit)? = null
    ) {
        scope.launch {
            try {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel,
                    duration = duration,
                    withDismissAction = true
                )

                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        onAction?.invoke()
                    }
                    SnackbarResult.Dismissed -> {
                        // Snackbar dismissed
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to show snackbar: $message")
            }
        }
    }
}

/**
 * Common snackbar messages
 */
object SnackbarMessages {
    // Success messages
    const val SAVED_TO_HISTORY = "Saved to history"
    const val COPIED_TO_CLIPBOARD = "Copied to clipboard"
    const val CODE_GENERATED = "QR code generated"
    const val DELETED_SUCCESSFULLY = "Deleted successfully"
    const val SETTINGS_SAVED = "Settings saved"

    // Error messages
    const val SCAN_FAILED = "Failed to scan code"
    const val GENERATION_FAILED = "Failed to generate code"
    const val SAVE_FAILED = "Failed to save"
    const val DELETE_FAILED = "Failed to delete"
    const val CAMERA_ERROR = "Camera error occurred"
    const val PERMISSION_DENIED = "Permission denied"
    const val INVALID_INPUT = "Invalid input"
    const val NETWORK_ERROR = "Network error. Check your connection"

    // Info messages
    const val NO_ITEMS_SELECTED = "No items selected"
    const val NO_CAMERA_AVAILABLE = "No camera available"
    const val NO_HISTORY = "No history available"

    // Warning messages
    const val STORAGE_FULL = "Storage is almost full"
    const val LARGE_DATASET = "Large dataset may take time to load"
}
