package com.rejown.qrcraft.presentation.generator

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.rejown.qrcraft.presentation.generator.components.CodePreview
import com.rejown.qrcraft.presentation.generator.components.ContentTypeSelector
import com.rejown.qrcraft.presentation.generator.components.CustomizationPanel
import com.rejown.qrcraft.presentation.generator.components.InputForm
import com.rejown.qrcraft.presentation.generator.state.GeneratorEvent
import com.rejown.qrcraft.utils.rememberHapticFeedback
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate QR Code") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Content type selector
            ContentTypeSelector(
                selectedType = state.selectedContentType,
                onTypeSelected = { type ->
                    viewModel.onEvent(GeneratorEvent.OnContentTypeSelected(type))
                    haptic.lightClick()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Input form
            InputForm(
                contentType = state.selectedContentType,
                content = state.inputContent,
                onContentChange = { content ->
                    viewModel.onEvent(GeneratorEvent.OnInputChanged(content))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Customization panel
            CustomizationPanel(
                isExpanded = state.showCustomization,
                customization = state.customization,
                onToggleExpanded = {
                    viewModel.onEvent(GeneratorEvent.OnToggleCustomization)
                },
                onCustomizationChanged = { customization ->
                    viewModel.onEvent(GeneratorEvent.OnCustomizationChanged(customization))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Generate button
            Button(
                onClick = {
                    viewModel.onEvent(GeneratorEvent.OnGenerateClicked)
                    haptic.mediumClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = state.inputContent.isNotEmpty() && !state.isGenerating
            ) {
                Text("Generate QR Code")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Code preview
            CodePreview(
                bitmap = state.generatedCode?.bitmap,
                isGenerating = state.isGenerating,
                error = state.error
            )

            // Action buttons (only show when code is generated)
            if (state.generatedCode != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            state.generatedCode?.bitmap?.let { bitmap ->
                                shareImage(context, bitmap)
                                haptic.lightClick()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.onEvent(GeneratorEvent.OnSaveClicked)
                            haptic.success()
                            Toast.makeText(
                                context,
                                "Saved to history",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun shareImage(context: Context, bitmap: Bitmap) {
    try {
        // Save bitmap to cache directory
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "qr_code_${System.currentTimeMillis()}.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share image", Toast.LENGTH_SHORT).show()
    }
}
