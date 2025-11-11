package com.rejown.qrcraft.presentation.generator.creation.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rejown.qrcraft.domain.models.BarcodeFormat

@Composable
fun CodePreview(
    bitmap: Bitmap?,
    isGenerating: Boolean,
    error: String?,
    selectedFormat: BarcodeFormat?,
    modifier: Modifier = Modifier
) {
    val is1D = selectedFormat?.let { is1DFormat(it) } ?: false

    Card(
        modifier = modifier.then(
            if (is1D) {
                // 1D codes (linear barcodes) - rectangle
                Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            } else {
                // 2D codes (QR, Data Matrix, Aztec, etc.) - smaller square
                Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isGenerating -> {
                    CircularProgressIndicator()
                }

                error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                bitmap != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Generated code",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(androidx.compose.ui.graphics.Color.White)
                                .padding(8.dp)
                        )
                    }
                }

                else -> {
                    Text(
                        text = "Preview will appear as you type",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

private fun is1DFormat(format: BarcodeFormat): Boolean {
    return format in listOf(
        BarcodeFormat.CODE_128,
        BarcodeFormat.CODE_39,
        BarcodeFormat.EAN_8,
        BarcodeFormat.EAN_13,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.CODABAR,
        BarcodeFormat.ITF
    )
}
