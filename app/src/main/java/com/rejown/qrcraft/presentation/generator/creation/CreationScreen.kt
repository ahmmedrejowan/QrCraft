package com.rejown.qrcraft.presentation.generator.creation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import kotlinx.coroutines.launch
import com.rejown.qrcraft.presentation.generator.creation.components.CodePreview
import com.rejown.qrcraft.presentation.generator.creation.components.CustomizationSheet
import com.rejown.qrcraft.presentation.generator.creation.components.DynamicInputForm
import com.rejown.qrcraft.presentation.generator.creation.components.FormatSelectionSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationScreen(
    templateId: String,
    codeId: Long? = null, // For edit mode
    onSaved: (Long) -> Unit,
    onBackPressed: () -> Unit,
    viewModel: CreationViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load template or existing code on first composition
    LaunchedEffect(templateId, codeId) {
        if (codeId != null) {
            viewModel.loadExistingCode(codeId)
        } else {
            viewModel.loadTemplate(templateId)
        }
    }

    // Show snackbar for success/error messages
    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearMessages()
        }
    }

    // Exit confirmation dialog
    if (state.showExitDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideExitDialog,
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to leave?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hideExitDialog()
                        onBackPressed()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideExitDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.template?.name ?: "Create",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!viewModel.handleBackPress()) {
                                onBackPressed()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.template == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error ?: "Template not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            CreationContent(
                state = state,
                onFieldValueChange = viewModel::updateFieldValue,
                onTitleChange = viewModel::updateTitle,
                onNoteChange = viewModel::updateNote,
                onFormatClick = viewModel::showFormatSheet,
                onCustomizationClick = viewModel::showCustomizationSheet,
                onSave = {
                    if (viewModel.validateAllFields()) {
                        scope.launch {
                            val codeId = viewModel.saveCode()
                            if (codeId != null) {
                                onSaved(codeId)
                            }
                        }
                    }
                },
                onShare = {
                    if (viewModel.validateAllFields()) {
                        scope.launch {
                            val uri = viewModel.getShareUri()
                            if (uri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "Share QR Code")
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )

            // Format Selection Sheet
            if (state.showFormatSheet) {
                state.template?.let { template ->
                    FormatSelectionSheet(
                        selectedFormat = state.selectedFormat,
                        allowedFormats = template.allowedFormats,
                        defaultFormat = template.defaultFormat,
                        onFormatSelected = viewModel::updateFormat,
                        onDismiss = viewModel::hideFormatSheet
                    )
                }
            }

            // Customization Sheet
            if (state.showCustomizationSheet) {
                CustomizationSheet(
                    customization = state.customization,
                    onCustomizationChanged = viewModel::updateCustomization,
                    onDismiss = viewModel::hideCustomizationSheet
                )
            }
        }
    }
}

@Composable
private fun CreationContent(
    state: CreationState,
    onFieldValueChange: (String, String) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onFormatClick: () -> Unit,
    onCustomizationClick: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Code Preview
            CodePreview(
                bitmap = state.generatedBitmap,
                isGenerating = state.isGenerating,
                error = state.error,
                selectedFormat = state.selectedFormat,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Dynamic Input Fields
            state.template?.let { template ->
                DynamicInputForm(
                    fields = template.fields,
                    values = state.fieldValues,
                    errors = state.validationErrors,
                    onValueChange = onFieldValueChange
                )
            }

            // Capacity Indicator
            if (state.maxCapacity > 0) {
                CapacityIndicator(
                    currentLength = state.contentLength,
                    maxCapacity = state.maxCapacity,
                    percentage = state.capacityPercentage,
                    warning = state.capacityWarning
                )
            }

            // Format and Customization in same row (compact cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormatSelectionCard(
                    format = state.selectedFormat?.name ?: "Not selected",
                    onClick = onFormatClick,
                    modifier = Modifier.weight(1f)
                )

                CustomizationCard(
                    onClick = onCustomizationClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Title Field
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Title (Optional)") },
                placeholder = { Text("e.g., My Business Card") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Note Field
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { Text("Note (Optional)") },
                placeholder = { Text("Add any notes or description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Bottom spacer for breathing room
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Fixed bottom action buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = state.generatedBitmap != null && !state.isSaving && !state.isSharing
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (state.isEditMode) "Update" else "Save")
                    }
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    enabled = state.generatedBitmap != null && !state.isSaving && !state.isSharing
                ) {
                    if (state.isSharing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Share")
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatSelectionCard(
    format: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Format",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = format,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CustomizationCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Customize",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Style & Colors",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CapacityIndicator(
    currentLength: Int,
    maxCapacity: Int,
    percentage: Float,
    warning: String?,
    modifier: Modifier = Modifier
) {
    val color = when {
        warning != null && percentage >= 100f -> MaterialTheme.colorScheme.error
        percentage >= 90f -> MaterialTheme.colorScheme.error
        percentage >= 75f -> Color(0xFFFF9800) // Orange warning
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (warning != null && percentage >= 90f) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Content Capacity",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currentLength / $maxCapacity",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(3.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (percentage / 100f).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(
                            color = color,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }

            // Warning message
            if (warning != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (percentage >= 100f) {
                            Icons.Default.Error
                        } else {
                            Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = warning,
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
