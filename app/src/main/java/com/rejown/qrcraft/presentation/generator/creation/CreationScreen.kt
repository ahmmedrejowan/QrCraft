package com.rejown.qrcraft.presentation.generator.creation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                actions = {
                    IconButton(onClick = { /* TODO: Show menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
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
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Code Preview
        CodePreview(
            bitmap = state.generatedBitmap,
            isGenerating = state.isGenerating,
            error = state.error,
            selectedFormat = state.selectedFormat
        )

        // Format Selection Row
        FormatSelectionRow(
            format = state.selectedFormat?.name ?: "Not selected",
            onClick = onFormatClick
        )

        // Customization Row
        CustomizationRow(onClick = onCustomizationClick)

        Divider()

        // Dynamic Input Fields
        state.template?.let { template ->
            DynamicInputForm(
                fields = template.fields,
                values = state.fieldValues,
                errors = state.validationErrors,
                onValueChange = onFieldValueChange
            )
        }

        Divider()

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

        Divider()

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    Text("Save")
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FormatSelectionRow(
    format: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Format",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = format,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        TextButton(onClick = onClick) {
            Text("Edit")
        }
    }
}

@Composable
private fun CustomizationRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Customization",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        TextButton(onClick = onClick) {
            Text("Edit")
        }
    }
}
