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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.rejown.qrcraft.presentation.scanner.state.ScannerScreenState
import com.rejown.qrcraft.presentation.scanner.state.ScanningState
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
                    } else {
                        // Try ZXing fallback
                        val bitmap = android.graphics.BitmapFactory.decodeStream(
                            context.contentResolver.openInputStream(it)
                        )
                        val zxingResult = com.rejown.qrcraft.utils.scanner.ImageDecoder.decodeWithZXing(bitmap)
                        if (zxingResult != null) {
                            viewModel.onEvent(ScannerEvent.OnBarcodeDetected(zxingResult))
                        } else {
                            Toast.makeText(context, "No code found in image", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to scan image: ${e.message}", Toast.LENGTH_SHORT).show()
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
    LaunchedEffect(state.scanningState) {
        timber.log.Timber.tag("QC ScannerScreen").d("LaunchedEffect - Triggered with state: ${state.scanningState::class.simpleName}")
        if (state.scanningState is com.rejown.qrcraft.presentation.scanner.state.ScanningState.Success) {
            val result = (state.scanningState as com.rejown.qrcraft.presentation.scanner.state.ScanningState.Success).result
            timber.log.Timber.tag("QC ScannerScreen").d("LaunchedEffect - Success state detected, navigating to detail. Result: ${result.displayValue}")
            onNavigateToDetail()
            timber.log.Timber.tag("QC ScannerScreen").d("LaunchedEffect - Navigation to detail completed")
        } else if (state.scanningState is com.rejown.qrcraft.presentation.scanner.state.ScanningState.Error) {
            timber.log.Timber.tag("QC ScannerScreen").d("LaunchedEffect - Error state detected")
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
                        }
                    )
                }

                !state.isPreviewActive -> {
                    // Preview manually stopped - show start button
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideocamOff,
                                contentDescription = "Camera Off",
                                modifier = Modifier.padding(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Camera Preview Paused",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Tap toggle to resume",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            ExtendedFloatingActionButton(
                                onClick = {
                                    viewModel.onEvent(ScannerEvent.StartPreview)
                                },
                                icon = {
                                    Icon(Icons.Default.Videocam, "Start Preview")
                                },
                                text = { Text("Start Preview") }
                            )
                        }
                    }
                }

                state.scanningState is com.rejown.qrcraft.presentation.scanner.state.ScanningState.Scanning -> {
                    // Camera preview with overlay
                    CameraPreview(
                        onBarcodeDetected = { result ->
                            viewModel.onEvent(ScannerEvent.OnBarcodeDetected(result))
                        },
                        isFlashlightOn = isFlashlightOn,
                        isPreviewActive = state.isPreviewActive
                    )
                    ScanOverlay()
                }

                state.scanningState is com.rejown.qrcraft.presentation.scanner.state.ScanningState.Success -> {
                    // Show camera preview even when Success (when navigating back from detail)
                    // This prevents white flash/blank screen
                    CameraPreview(
                        onBarcodeDetected = { result ->
                            viewModel.onEvent(ScannerEvent.OnBarcodeDetected(result))
                        },
                        isFlashlightOn = isFlashlightOn,
                        isPreviewActive = state.isPreviewActive
                    )
                    ScanOverlay()
                }

                state.scanningState is com.rejown.qrcraft.presentation.scanner.state.ScanningState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val errorMessage = (state.scanningState as com.rejown.qrcraft.presentation.scanner.state.ScanningState.Error).message
                        Text("Error: $errorMessage")
                    }
                }

                else -> {
                    // Idle or other states - show camera preview
                    CameraPreview(
                        onBarcodeDetected = { result ->
                            viewModel.onEvent(ScannerEvent.OnBarcodeDetected(result))
                        },
                        isFlashlightOn = isFlashlightOn,
                        isPreviewActive = state.isPreviewActive
                    )
                    ScanOverlay()
                }
            }

            // Show bottom sheet with scan result
            if (showBottomSheet && state.scanningState is com.rejown.qrcraft.presentation.scanner.state.ScanningState.Success) {
                val result = (state.scanningState as com.rejown.qrcraft.presentation.scanner.state.ScanningState.Success).result

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
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        shareContent(context, result.displayValue)
                    },
                    onOpen = {
                        if (result.contentType == ContentType.URL) {
                            openUrl(context, result.displayValue)
                        }
                    },
                    onSave = {
                        viewModel.onEvent(ScannerEvent.OnSaveToHistory)
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

        // Action buttons (Gallery + Flashlight + Preview Toggle)
        if (cameraPermissionState.status.isGranted) {
            // Preview Toggle button - Top right
            SmallFloatingActionButton(
                onClick = {
                    viewModel.onEvent(ScannerEvent.TogglePreview)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                containerColor = if (state.isPreviewActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Icon(
                    imageVector = if (state.isPreviewActive) {
                        Icons.Default.Videocam
                    } else {
                        Icons.Default.VideocamOff
                    },
                    contentDescription = if (state.isPreviewActive) "Pause camera preview" else "Resume camera preview",
                    tint = if (state.isPreviewActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }

            // Gallery button - Left bottom
            androidx.compose.material3.IconButton(
                onClick = {
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
