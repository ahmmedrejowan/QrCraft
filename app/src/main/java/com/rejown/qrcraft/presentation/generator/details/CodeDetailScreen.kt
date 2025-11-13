package com.rejown.qrcraft.presentation.generator.details

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                    }
                }
            )
        }
    }

    // Copy bottom sheet
    // Copy bottom sheet removed - copy button now directly copies content

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
                    // Delete button
                    IconButton(
                        onClick = {
                            viewModel.showDeleteDialog()
                        },
                        enabled = state.code != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    // Favorite button
                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite()
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
                            viewModel.copyContent()
                        },
                        onShare = {
                            viewModel.showShareBottomSheet()
                        },
                        onSave = {
                            scope.launch {
                                viewModel.saveToGallery()
                            }
                        },
                        onOpen = getOpenAction(code, context, scope, snackbarHostState),
                        onEdit = onEdit?.let { editFn ->
                            {
                                editFn(code.templateId)
                            }
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

private fun getOpenAction(
    code: com.rejown.qrcraft.domain.models.GeneratedCodeData,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
): (() -> Unit)? {
    val content = code.formattedContent
    val type = code.barcodeType.uppercase()

    // Determine if this content type should have an action button
    return when {
        type.contains("URL") || type.contains("LINK") ||
        content.startsWith("http://", ignoreCase = true) ||
        content.startsWith("https://", ignoreCase = true) -> {
            {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(content))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Unable to open URL")
                    }
                }
            }
        }
        type.contains("EMAIL") || content.startsWith("mailto:", ignoreCase = true) -> {
            {
                try {
                    val emailUri = if (content.startsWith("mailto:")) content else "mailto:$content"
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(emailUri))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Unable to open email")
                    }
                }
            }
        }
        type.contains("PHONE") || type.contains("TEL") || content.startsWith("tel:", ignoreCase = true) -> {
            {
                try {
                    val telUri = if (content.startsWith("tel:")) content else "tel:$content"
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(telUri))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Unable to open dialer")
                    }
                }
            }
        }
        type.contains("SMS") || content.startsWith("sms:", ignoreCase = true) || content.startsWith("smsto:", ignoreCase = true) -> {
            {
                try {
                    val smsUri = when {
                        content.startsWith("sms:") || content.startsWith("smsto:") -> content
                        else -> "sms:$content"
                    }
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(smsUri))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Unable to open messaging")
                    }
                }
            }
        }
        type.contains("WIFI") -> null // WiFi doesn't have a simple intent action
        else -> null // Plain text, vCard, etc. don't need an open action
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
    modifier: Modifier = Modifier
) {
    val code = state.code ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // QR Code Image - Adaptive size based on barcode type
        val is2DBarcode = is2DBarcodeFormat(code.barcodeFormat.name)
        val previewModifier = if (is2DBarcode) {
            // 2D codes (QR, Data Matrix, Aztec, etc.) - smaller square
            Modifier
                .fillMaxWidth(0.75f) // 75% of screen width
                .aspectRatio(1f)
                .align(Alignment.CenterHorizontally)
        } else {
            // 1D codes (linear barcodes) - rectangle with same height as reduced square
            Modifier
                .fillMaxWidth()
                .height(240.dp) // Same as 75% width on most phones
        }

        Card(
            modifier = previewModifier,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                state.bitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated ${if (is2DBarcode) "QR" else "Barcode"}",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (is2DBarcode) 32.dp else 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(if (is2DBarcode) 20.dp else 16.dp)
                    )
                } ?: Text(
                    text = "Image not available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Content - MOVED HERE (below preview)
        ContentSection(content = code.formattedContent)

        // Note (if exists) - RIGHT AFTER CONTENT
        code.note?.let { note ->
            NoteSection(note = note)
        }

        // Action Buttons - Row 1 (Primary Actions)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Open/Action button (only show if applicable) - PRIMARY ACTION
            if (onOpen != null) {
                val type = code.barcodeType.uppercase()
                val content = code.formattedContent
                val (actionIcon, actionText) = when {
                    type.contains("URL") || type.contains("LINK") || content.startsWith("http") ->
                        Icons.Default.OpenInBrowser to "Open"
                    type.contains("EMAIL") || content.startsWith("mailto:") ->
                        Icons.Default.Email to "Email"
                    type.contains("PHONE") || type.contains("TEL") || content.startsWith("tel:") ->
                        Icons.Default.Phone to "Call"
                    type.contains("SMS") || content.startsWith("sms:") ->
                        Icons.AutoMirrored.Filled.Message to "Message"
                    else -> Icons.AutoMirrored.Filled.OpenInNew to "Open"
                }

                Button(
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

            // Copy
            OutlinedButton(
                onClick = onCopy,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy")
            }
        }

        // Action Buttons - Row 2 (Secondary Actions)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

            // Edit
            if (onEdit != null) {
                OutlinedButton(
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
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Template Info - Redesigned
        TemplateInfoSection(
            templateName = code.templateName,
            barcodeType = code.barcodeType,
            barcodeFormat = code.barcodeFormat.name
        )

        // Customization - Redesigned
        CustomizationSection(
            size = code.size,
            margin = code.margin,
            foregroundColor = code.foregroundColor,
            backgroundColor = code.backgroundColor,
            errorCorrection = code.errorCorrection?.name
        )

        // Metadata - Redesigned
        MetadataSection(
            createdAt = code.createdAt,
            updatedAt = code.updatedAt,
            scanCount = code.scanCount
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ContentSection(content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Subject,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Content",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TemplateInfoSection(
    templateName: String,
    barcodeType: String,
    barcodeFormat: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Template Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DetailRow(label = "Template", value = templateName)
            DetailRow(label = "Type", value = barcodeType)
            DetailRow(label = "Format", value = barcodeFormat)
        }
    }
}

@Composable
private fun NoteSection(note: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Note,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Note",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CustomizationSection(
    size: Int,
    margin: Int,
    foregroundColor: Int,
    backgroundColor: Int,
    errorCorrection: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Customization",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DetailRow(label = "Size", value = "${size}px")
            DetailRow(label = "Margin", value = margin.toString())

            // Color preview row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Colors",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Foreground color
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(foregroundColor))
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Background color
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(backgroundColor))
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }

            errorCorrection?.let {
                DetailRow(label = "Error Correction", value = it)
            }
        }
    }
}

@Composable
private fun MetadataSection(
    createdAt: Long,
    updatedAt: Long,
    scanCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DetailRow(label = "Created", value = formatDate(createdAt))
            DetailRow(label = "Modified", value = formatDate(updatedAt))
            DetailRow(label = "Scan Count", value = scanCount.toString())
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
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
                    containerColor = Color.Transparent
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
                    containerColor = Color.Transparent
                )
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun is2DBarcodeFormat(format: String): Boolean {
    return when (format.uppercase()) {
        "QR_CODE", "DATA_MATRIX", "AZTEC", "PDF_417", "MAXICODE" -> true
        else -> false // CODE_128, EAN_13, EAN_8, UPC_A, UPC_E, CODE_39, CODE_93, CODABAR, ITF
    }
}
