package com.rejown.qrcraft.presentation.scanner.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.rejown.qrcraft.domain.models.ContentType
import com.rejown.qrcraft.domain.models.ScanResult
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailScreen(
    scanResult: ScanResult,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    timber.log.Timber.tag("QRCraft ScanDetailScreen").e("composable - Composing with result: ${scanResult.displayValue}, type: ${scanResult.contentType}")

    val context = LocalContext.current

    // Handle device back button/gesture
    BackHandler {
        timber.log.Timber.tag("QRCraft ScanDetailScreen").e("BackHandler - Device back triggered")
        onBack()
    }

    DisposableEffect(Unit) {
        timber.log.Timber.tag("QRCraft ScanDetailScreen").e("DisposableEffect - Screen entered")
        onDispose {
            timber.log.Timber.tag("QRCraft ScanDetailScreen").e("DisposableEffect - Screen disposed")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = scanResult.contentType.displayName,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // QR Code Preview
            val qrBitmap = remember(scanResult.rawValue) {
                generateQRCode(scanResult.rawValue, scanResult.format)
            }

            if (qrBitmap != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Saved to history message
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Saved to history",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Content Display Card
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Content",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = scanResult.displayValue,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Always available actions: Copy and Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        copyToClipboard(context, scanResult.displayValue)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy")
                }

                OutlinedButton(
                    onClick = {
                        shareContent(context, scanResult.displayValue)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
            }

            // Content-type specific actions
            ContentSpecificActions(
                scanResult = scanResult,
                context = context
            )

            HorizontalDivider()

            // Scan Information
            InfoSection(title = "Scan Information") {
                InfoRow(label = "Format", value = scanResult.format.displayName)
                InfoRow(label = "Type", value = scanResult.contentType.displayName)
                InfoRow(
                    label = "Scanned",
                    value = formatDate(scanResult.timestamp)
                )
            }

            // Raw value (if different from display value)
            if (scanResult.rawValue != scanResult.displayValue) {
                InfoSection(title = "Raw Value") {
                    Text(
                        text = scanResult.rawValue,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ContentSpecificActions(
    scanResult: ScanResult,
    context: Context,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (scanResult.contentType) {
            ContentType.URL -> {
                FilledTonalButton(
                    onClick = { openUrl(context, scanResult.displayValue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open in Browser")
                }
            }

            ContentType.EMAIL -> {
                FilledTonalButton(
                    onClick = { sendEmail(context, scanResult.displayValue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Email")
                }
            }

            ContentType.PHONE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { dialPhone(context, scanResult.displayValue) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call")
                    }

                    OutlinedButton(
                        onClick = { sendSms(context, scanResult.displayValue) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SMS")
                    }
                }
            }

            ContentType.SMS -> {
                FilledTonalButton(
                    onClick = { sendSms(context, scanResult.displayValue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send SMS")
                }
            }

            ContentType.GEO -> {
                FilledTonalButton(
                    onClick = { openMap(context, scanResult.displayValue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open in Maps")
                }
            }

            ContentType.WIFI -> {
                FilledTonalButton(
                    onClick = {
                        // WiFi connection would require additional permissions and handling
                        Toast.makeText(context, "WiFi details copied to clipboard", Toast.LENGTH_SHORT).show()
                        copyToClipboard(context, scanResult.displayValue)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy WiFi Details")
                }
            }

            ContentType.CONTACT -> {
                FilledTonalButton(
                    onClick = { addContact(context, scanResult.displayValue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Contact")
                }
            }

            ContentType.CALENDAR -> {
                FilledTonalButton(
                    onClick = { addCalendarEvent(context, scanResult.displayValue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Calendar")
                }
            }

            ContentType.PRODUCT -> {
                FilledTonalButton(
                    onClick = { searchProduct(context, scanResult.displayValue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search Product")
                }
            }

            else -> {
                // No specific action for TEXT, CRYPTO, etc.
                // Copy and Share are already available above
            }
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

// Helper functions for content-specific actions
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("QR Code Content", text)
    clipboard.setPrimaryClip(clip)
}

private fun shareContent(context: Context, text: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
    }
}

private fun sendEmail(context: Context, email: String) {
    val emailAddress = if (email.startsWith("mailto:")) {
        email.substring(7)
    } else {
        email
    }

    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
    }
}

private fun dialPhone(context: Context, phone: String) {
    val phoneNumber = if (phone.startsWith("tel:")) {
        phone.substring(4)
    } else {
        phone
    }

    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show()
    }
}

private fun sendSms(context: Context, phone: String) {
    val phoneNumber = phone.replace("sms:", "").replace("smsto:", "")

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open SMS app", Toast.LENGTH_SHORT).show()
    }
}

private fun openMap(context: Context, geo: String) {
    try {
        val geoUri = if (geo.startsWith("geo:")) geo else "geo:$geo"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open maps", Toast.LENGTH_SHORT).show()
    }
}

private fun addContact(context: Context, vcard: String) {
    try {
        val intent = Intent(Intent.ACTION_INSERT, android.provider.ContactsContract.Contacts.CONTENT_URI)
        // Parse vCard and extract fields would go here
        // For now, just show a toast
        Toast.makeText(context, "Contact details copied", Toast.LENGTH_SHORT).show()
        copyToClipboard(context, vcard)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot add contact", Toast.LENGTH_SHORT).show()
    }
}

private fun addCalendarEvent(context: Context, vevent: String) {
    try {
        val intent = Intent(Intent.ACTION_INSERT, android.provider.CalendarContract.Events.CONTENT_URI)
        // Parse vEvent and extract fields would go here
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot add calendar event", Toast.LENGTH_SHORT).show()
    }
}

private fun searchProduct(context: Context, productCode: String) {
    try {
        val searchUrl = "https://www.google.com/search?q=$productCode"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot search product", Toast.LENGTH_SHORT).show()
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun generateQRCode(content: String, format: com.rejown.qrcraft.domain.models.BarcodeFormat): Bitmap? {
    return try {
        // Map domain BarcodeFormat to ZXing BarcodeFormat
        val zxingFormat = when (format) {
            com.rejown.qrcraft.domain.models.BarcodeFormat.QR_CODE -> BarcodeFormat.QR_CODE
            com.rejown.qrcraft.domain.models.BarcodeFormat.CODE_128 -> BarcodeFormat.CODE_128
            com.rejown.qrcraft.domain.models.BarcodeFormat.CODE_39 -> BarcodeFormat.CODE_39
            com.rejown.qrcraft.domain.models.BarcodeFormat.EAN_13 -> BarcodeFormat.EAN_13
            com.rejown.qrcraft.domain.models.BarcodeFormat.EAN_8 -> BarcodeFormat.EAN_8
            com.rejown.qrcraft.domain.models.BarcodeFormat.UPC_A -> BarcodeFormat.UPC_A
            com.rejown.qrcraft.domain.models.BarcodeFormat.UPC_E -> BarcodeFormat.UPC_E
            com.rejown.qrcraft.domain.models.BarcodeFormat.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            com.rejown.qrcraft.domain.models.BarcodeFormat.PDF417 -> BarcodeFormat.PDF_417
            com.rejown.qrcraft.domain.models.BarcodeFormat.AZTEC -> BarcodeFormat.AZTEC
            com.rejown.qrcraft.domain.models.BarcodeFormat.CODABAR -> BarcodeFormat.CODABAR
            com.rejown.qrcraft.domain.models.BarcodeFormat.ITF -> BarcodeFormat.ITF
            com.rejown.qrcraft.domain.models.BarcodeFormat.CODE_93 -> BarcodeFormat.CODE_93
            com.rejown.qrcraft.domain.models.BarcodeFormat.UNKNOWN -> BarcodeFormat.QR_CODE // Default to QR_CODE for unknown
        }

        val size = 512
        val writer = MultiFormatWriter()
        val bitMatrix: BitMatrix = writer.encode(content, zxingFormat, size, size)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }

        bitmap
    } catch (e: Exception) {
        timber.log.Timber.tag("QRCraft ScanDetailScreen").e(e, "generateQRCode - Failed to generate QR code")
        null
    }
}
