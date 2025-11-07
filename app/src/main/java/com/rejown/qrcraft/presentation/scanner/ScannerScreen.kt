package com.rejown.qrcraft.presentation.scanner

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.rejown.qrcraft.domain.models.ContentType
import com.rejown.qrcraft.presentation.scanner.components.CameraPreview
import com.rejown.qrcraft.presentation.scanner.components.ScanOverlay
import com.rejown.qrcraft.presentation.scanner.components.ScanResultBottomSheet
import com.rejown.qrcraft.presentation.scanner.state.ScannerEvent
import com.rejown.qrcraft.presentation.scanner.state.ScannerState
import com.rejown.qrcraft.utils.rememberHapticFeedback
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var isFlashlightOn by remember { mutableStateOf(false) }

    // Request camera permission on first launch
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // Show bottom sheet when scan is successful
    LaunchedEffect(state) {
        when (state) {
            is ScannerState.Success -> {
                showBottomSheet = true
                haptic.success()
            }
            is ScannerState.Error -> {
                haptic.error()
            }
            else -> {}
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Main content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                !cameraPermissionState.status.isGranted -> {
                    // Permission denied
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Camera permission is required to scan QR codes")
                    }
                }

                state is ScannerState.Scanning -> {
                    // Camera preview with overlay
                    CameraPreview(
                        onBarcodeDetected = { result ->
                            viewModel.onEvent(ScannerEvent.OnBarcodeDetected(result))
                        },
                        isFlashlightOn = isFlashlightOn
                    )
                    ScanOverlay()
                }

                state is ScannerState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: ${(state as ScannerState.Error).message}")
                    }
                }
            }

            // Show bottom sheet with scan result
            if (showBottomSheet && state is ScannerState.Success) {
                val result = (state as ScannerState.Success).result

                ScanResultBottomSheet(
                    scanResult = result,
                    sheetState = sheetState,
                    onDismiss = {
                        showBottomSheet = false
                        scope.launch {
                            sheetState.hide()
                            viewModel.onEvent(ScannerEvent.OnResultDismissed)
                        }
                    },
                    onCopy = {
                        copyToClipboard(context, result.displayValue)
                        haptic.lightClick()
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        shareContent(context, result.displayValue)
                        haptic.lightClick()
                    },
                    onOpen = {
                        if (result.contentType == ContentType.URL) {
                            openUrl(context, result.displayValue)
                        }
                    },
                    onSave = {
                        viewModel.onEvent(ScannerEvent.OnSaveToHistory)
                        haptic.success()
                        Toast.makeText(context, "Saved to history", Toast.LENGTH_SHORT).show()
                        showBottomSheet = false
                        scope.launch {
                            sheetState.hide()
                            viewModel.onEvent(ScannerEvent.OnResultDismissed)
                        }
                    },
                    onDelete = {
                        showBottomSheet = false
                        scope.launch {
                            sheetState.hide()
                            viewModel.onEvent(ScannerEvent.OnResultDismissed)
                        }
                    }
                )
            }
        }

        // Flashlight FAB
        if (cameraPermissionState.status.isGranted) {
            FloatingActionButton(
                onClick = {
                    isFlashlightOn = !isFlashlightOn
                    haptic.mediumClick()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = if (isFlashlightOn) {
                        Icons.Default.FlashlightOn
                    } else {
                        Icons.Default.FlashlightOff
                    },
                    contentDescription = if (isFlashlightOn) "Turn off flashlight" else "Turn on flashlight"
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("QR Code", text)
    clipboard.setPrimaryClip(clip)
}

private fun shareContent(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to open URL", Toast.LENGTH_SHORT).show()
    }
}
