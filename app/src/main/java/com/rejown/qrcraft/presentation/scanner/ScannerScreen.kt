package com.rejown.qrcraft.presentation.scanner

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
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
import com.google.accompanist.permissions.shouldShowRationale
import com.rejown.qrcraft.domain.models.ContentType
import com.rejown.qrcraft.presentation.scanner.components.CameraPreview
import com.rejown.qrcraft.presentation.scanner.components.PermissionDeniedContent
import com.rejown.qrcraft.presentation.scanner.components.PermissionRationaleSheet
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
    onNavigateToDetail: () -> Unit = {},
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

    // Permission states
    val permissionRationaleSheetState = rememberModalBottomSheetState()
    var showPermissionRationale by remember { mutableStateOf(false) }
    var hasCheckedPermission by remember { mutableStateOf(false) }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val inputImage = com.google.mlkit.vision.common.InputImage.fromFilePath(context, it)
                    val result = com.rejown.qrcraft.utils.scanner.ImageDecoder.decodeWithMLKit(inputImage)
                    if (result != null) {
                        viewModel.onEvent(ScannerEvent.OnBarcodeDetected(result))
                        haptic.success()
                    } else {
                        // Try ZXing fallback
                        val bitmap = android.graphics.BitmapFactory.decodeStream(
                            context.contentResolver.openInputStream(it)
                        )
                        val zxingResult = com.rejown.qrcraft.utils.scanner.ImageDecoder.decodeWithZXing(bitmap)
                        if (zxingResult != null) {
                            viewModel.onEvent(ScannerEvent.OnBarcodeDetected(zxingResult))
                            haptic.success()
                        } else {
                            Toast.makeText(context, "No code found in image", Toast.LENGTH_SHORT).show()
                            haptic.error()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to scan image: ${e.message}", Toast.LENGTH_SHORT).show()
                    haptic.error()
                }
            }
        }
    }

    // Check and request camera permission with better UX
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted && !hasCheckedPermission) {
            // Show rationale first instead of directly requesting
            // Don't set hasCheckedPermission here - wait until user actually responds
            showPermissionRationale = true
        }
    }

    // Navigate to detail when scan is successful (only once per success)
    LaunchedEffect(state) {
        timber.log.Timber.tag("QRCraft ScannerScreen").e("LaunchedEffect - Triggered with state: ${state::class.simpleName}")
        if (state is ScannerState.Success) {
            timber.log.Timber.tag("QRCraft ScannerScreen").e("LaunchedEffect - Success state detected, navigating to detail. Result: ${(state as ScannerState.Success).result.displayValue}")
            haptic.success()
            onNavigateToDetail()
            timber.log.Timber.tag("QRCraft ScannerScreen").e("LaunchedEffect - Navigation to detail completed")
        } else if (state is ScannerState.Error) {
            timber.log.Timber.tag("QRCraft ScannerScreen").e("LaunchedEffect - Error state detected")
            haptic.error()
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
                    // Check if permission is permanently denied
                    // Permanently denied = user denied AND shouldShowRationale is false AND we've asked before
                    val isPermanentlyDenied = !cameraPermissionState.status.shouldShowRationale &&
                                             hasCheckedPermission &&
                                             !showPermissionRationale

                    // Permission denied - show helpful UI
                    PermissionDeniedContent(
                        isPermanentlyDenied = isPermanentlyDenied,
                        onRequestPermission = {
                            if (isPermanentlyDenied) {
                                // User has permanently denied permission, open settings
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            } else {
                                // Normal permission request
                                hasCheckedPermission = true
                                cameraPermissionState.launchPermissionRequest()
                            }
                            haptic.lightClick()
                        }
                    )
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

                state is ScannerState.Success -> {
                    // Show camera preview even when Success (when navigating back from detail)
                    // This prevents white flash/blank screen
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

                else -> {
                    // Idle or other states - show camera preview
                    CameraPreview(
                        onBarcodeDetected = { result ->
                            viewModel.onEvent(ScannerEvent.OnBarcodeDetected(result))
                        },
                        isFlashlightOn = isFlashlightOn
                    )
                    ScanOverlay()
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

        // Action buttons (Gallery + Flashlight)
        if (cameraPermissionState.status.isGranted) {
            // Gallery button - Left bottom
            androidx.compose.material3.IconButton(
                onClick = {
                    haptic.lightClick()
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Scan from gallery",
                    modifier = Modifier.padding(8.dp),
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }

            // Flashlight button - Right bottom
            androidx.compose.material3.IconButton(
                onClick = {
                    isFlashlightOn = !isFlashlightOn
                    haptic.mediumClick()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = if (isFlashlightOn) {
                        Icons.Default.FlashlightOn
                    } else {
                        Icons.Default.FlashlightOff
                    },
                    contentDescription = if (isFlashlightOn) "Turn off flashlight" else "Turn on flashlight",
                    modifier = Modifier.padding(8.dp),
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }

        // Permission rationale bottom sheet
        if (showPermissionRationale) {
            // On first launch, shouldShowRationale is false, so don't treat it as permanently denied
            // Only permanently denied if user has actually been asked before
            val isPermanentlyDeniedInSheet = !cameraPermissionState.status.shouldShowRationale &&
                                            hasCheckedPermission

            PermissionRationaleSheet(
                sheetState = permissionRationaleSheetState,
                isPermanentlyDenied = isPermanentlyDeniedInSheet,
                onDismiss = {
                    showPermissionRationale = false
                    scope.launch {
                        permissionRationaleSheetState.hide()
                    }
                },
                onRequestPermission = {
                    showPermissionRationale = false
                    scope.launch {
                        permissionRationaleSheetState.hide()
                    }

                    if (isPermanentlyDeniedInSheet) {
                        // User has permanently denied permission, open settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        // Mark that we've requested permission at least once
                        hasCheckedPermission = true
                        cameraPermissionState.launchPermissionRequest()
                    }
                    haptic.lightClick()
                }
            )
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
