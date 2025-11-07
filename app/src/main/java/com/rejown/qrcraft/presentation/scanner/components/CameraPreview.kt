package com.rejown.qrcraft.presentation.scanner.components

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.rejown.qrcraft.domain.models.ScanResult
import timber.log.Timber
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    onBarcodeDetected: (ScanResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    ) {
        startCamera(
            context = context,
            previewView = previewView,
            lifecycleOwner = lifecycleOwner,
            cameraExecutor = cameraExecutor,
            onBarcodeDetected = onBarcodeDetected
        )
    }
}

private fun startCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraExecutor: java.util.concurrent.ExecutorService,
    onBarcodeDetected: (ScanResult) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()

            // Preview use case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Image analysis use case for barcode scanning
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer(onBarcodeDetected))
                }

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            Timber.d("Camera started successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start camera")
        }
    }, ContextCompat.getMainExecutor(context))
}
