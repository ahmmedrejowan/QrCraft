package com.rejown.qrcraft.presentation.generator.details

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rejown.qrcraft.utils.rememberHapticFeedback
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeDetailScreen(
    codeId: Long,
    onEdit: ((String) -> Unit)? = null,
    onBack: () -> Unit,
    viewModel: CodeDetailViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = rememberHapticFeedback()

    // Load code on first composition
    LaunchedEffect(codeId) {
        viewModel.loadCode(codeId)
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

    // Delete confirmation dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteDialog,
            title = { Text("Delete QR Code?") },
            text = { Text("This action cannot be undone. The QR code and its image will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val deleted = viewModel.deleteCode()
                            if (deleted) {
                                onBack()
                            }
                        }
                        haptic.strongImpact()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    // Share bottom sheet
    if (state.showShareBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::hideShareBottomSheet,
            sheetState = rememberModalBottomSheetState()
        ) {
            ShareOptionsBottomSheet(
                onShareContent = {
                    scope.launch {
                        val code = state.code ?: return@launch
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, code.formattedContent)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Content"))
                        viewModel.hideShareBottomSheet()
                        haptic.lightClick()
                    }
                },
                onShareQRCode = {
                    scope.launch {
                        val uri = viewModel.shareQRImage()
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
                        haptic.lightClick()
                    }
                }
            )
        }
    }

    // Copy bottom sheet
    if (state.showCopyBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::hideCopyBottomSheet,
            sheetState = rememberModalBottomSheetState()
        ) {
            CopyOptionsBottomSheet(
                onCopyContent = {
                    viewModel.copyContent()
                    haptic.lightClick()
                },
                onCopyQRCode = {
                    scope.launch {
                        viewModel.copyQRImage()
                        haptic.lightClick()
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.code?.title ?: "QR Code Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite()
                            haptic.lightClick()
                        },
                        enabled = state.code != null
                    ) {
                        AnimatedVisibility(
                            visible = state.code?.isFavorite == true,
                            enter = scaleIn(spring(stiffness = Spring.StiffnessHigh)),
                            exit = scaleOut()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorited",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        AnimatedVisibility(
                            visible = state.code?.isFavorite == false,
                            enter = scaleIn(spring(stiffness = Spring.StiffnessHigh)),
                            exit = scaleOut()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Not favorited",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null && state.code == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onBack) {
                            Text("Go Back")
                        }
                    }
                }
            }

            else -> {
                state.code?.let { code ->
                    CodeDetailContent(
                        state = state,
                        onCopy = {
                            viewModel.showCopyBottomSheet()
                            haptic.lightClick()
                        },
                        onShare = {
                            viewModel.showShareBottomSheet()
                            haptic.lightClick()
                        },
                        onSave = {
                            scope.launch {
                                viewModel.saveToGallery()
                                haptic.success()
                            }
                        },
                        onOpen = {
                            // Smart action based on content type
                            val content = code.formattedContent
                            try {
                                when {
                                    content.startsWith("http://") || content.startsWith("https://") -> {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(content))
                                        context.startActivity(intent)
                                    }
                                    content.startsWith("mailto:") -> {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(content))
                                        context.startActivity(intent)
                                    }
                                    content.startsWith("tel:") -> {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(content))
                                        context.startActivity(intent)
                                    }
                                    content.startsWith("sms:") -> {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(content))
                                        context.startActivity(intent)
                                    }
                                    else -> {
                                        // Try as URL
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$content"))
                                        context.startActivity(intent)
                                    }
                                }
                                haptic.lightClick()
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Unable to open content")
                                }
                            }
                        },
                        onEdit = onEdit?.let { editFn ->
                            {
                                editFn(code.templateId)
                                haptic.lightClick()
                            }
                        },
                        onDelete = {
                            viewModel.showDeleteDialog()
                            haptic.lightClick()
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeDetailContent(
    state: CodeDetailState,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onOpen: (() -> Unit)?,
    onEdit: (() -> Unit)?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val code = state.code ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // QR Code Image
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                state.bitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(androidx.compose.ui.graphics.Color.White)
                            .padding(16.dp)
                    )
                } ?: Text(
                    text = "Image not available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Buttons - Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Copy
            OutlinedButton(
                onClick = onCopy,
                modifier = Modifier.weight(1f),
                enabled = !state.isCopying
            ) {
                if (state.isCopying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy")
            }

            // Share
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }

            // Save
            OutlinedButton(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !state.isSavingToGallery
            ) {
                if (state.isSavingToGallery) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save")
            }
        }

        // Action Buttons - Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Open/Action button (smart based on content type)
            if (onOpen != null) {
                val content = code.formattedContent
                val (actionIcon, actionText) = when {
                    content.startsWith("http://") || content.startsWith("https://") ->
                        Icons.Default.OpenInBrowser to "Open"
                    content.startsWith("mailto:") -> Icons.Default.Email to "Email"
                    content.startsWith("tel:") -> Icons.Default.Phone to "Call"
                    content.startsWith("sms:") -> Icons.Default.Message to "Message"
                    else -> Icons.Default.OpenInNew to "Open"
                }

                FilledTonalButton(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(actionText)
                }
            }

            // Edit
            if (onEdit != null) {
                FilledTonalButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit")
                }
            }

            // Delete
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete")
            }
        }

        HorizontalDivider()

        // Template Info
        InfoSection(title = "Template") {
            InfoRow(label = "Name", value = code.templateName)
            InfoRow(label = "Type", value = code.barcodeType)
            InfoRow(label = "Format", value = code.barcodeFormat)
        }

        // Content
        InfoSection(title = "Content") {
            Text(
                text = code.formattedContent,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        // Note (if exists)
        code.note?.let { note ->
            InfoSection(title = "Note") {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }

        // Customization
        InfoSection(title = "Customization") {
            InfoRow(label = "Size", value = "${code.size}px")
            InfoRow(label = "Margin", value = code.margin.toString())
            code.errorCorrection?.let {
                InfoRow(label = "Error Correction", value = it)
            }
        }

        // Metadata
        InfoSection(title = "Metadata") {
            InfoRow(
                label = "Created",
                value = formatDate(code.createdAt)
            )
            InfoRow(
                label = "Modified",
                value = formatDate(code.updatedAt)
            )
            InfoRow(label = "Scan Count", value = code.scanCount.toString())
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ShareOptionsBottomSheet(
    onShareContent: () -> Unit,
    onShareQRCode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Share",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            onClick = onShareContent,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            ListItem(
                headlineContent = { Text("Share Content") },
                supportingContent = { Text("Share the text content only") },
                leadingContent = {
                    Icon(Icons.Default.TextFields, contentDescription = null)
                },
                colors = ListItemDefaults.colors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }

        Surface(
            onClick = onShareQRCode,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            ListItem(
                headlineContent = { Text("Share QR Code") },
                supportingContent = { Text("Share the QR code image") },
                leadingContent = {
                    Icon(Icons.Default.QrCode2, contentDescription = null)
                },
                colors = ListItemDefaults.colors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun CopyOptionsBottomSheet(
    onCopyContent: () -> Unit,
    onCopyQRCode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Copy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            onClick = onCopyContent,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            ListItem(
                headlineContent = { Text("Copy Content") },
                supportingContent = { Text("Copy the text content to clipboard") },
                leadingContent = {
                    Icon(Icons.Default.TextFields, contentDescription = null)
                },
                colors = ListItemDefaults.colors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }

        Surface(
            onClick = onCopyQRCode,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            ListItem(
                headlineContent = { Text("Copy QR Code") },
                supportingContent = { Text("Copy the QR code image") },
                leadingContent = {
                    Icon(Icons.Default.QrCode2, contentDescription = null)
                },
                colors = ListItemDefaults.colors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
