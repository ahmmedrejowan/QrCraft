package com.rejown.qrcraft.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.ContentType

/**
 * Accessibility utilities for improving app usability with screen readers
 */
object AccessibilityUtils {

    /**
     * Get human-readable description for content type
     */
    fun getContentTypeDescription(contentType: ContentType): String {
        return when (contentType) {
            ContentType.URL -> "Website link"
            ContentType.EMAIL -> "Email address"
            ContentType.PHONE -> "Phone number"
            ContentType.SMS -> "Text message"
            ContentType.WIFI -> "WiFi network"
            ContentType.CONTACT -> "Contact information"
            ContentType.CALENDAR -> "Calendar event"
            ContentType.GEO -> "Geographic location"
            ContentType.PRODUCT -> "Product barcode"
            ContentType.CRYPTO -> "Cryptocurrency address"
            ContentType.TEXT -> "Plain text"
        }
    }

    /**
     * Get human-readable description for barcode format
     */
    fun getBarcodeFormatDescription(format: BarcodeFormat): String {
        return when (format) {
            BarcodeFormat.QR_CODE -> "QR Code"
            BarcodeFormat.AZTEC -> "Aztec code"
            BarcodeFormat.DATA_MATRIX -> "Data Matrix"
            BarcodeFormat.PDF417 -> "PDF417"
            BarcodeFormat.EAN_8 -> "EAN-8 barcode"
            BarcodeFormat.EAN_13 -> "EAN-13 barcode"
            BarcodeFormat.UPC_A -> "UPC-A barcode"
            BarcodeFormat.UPC_E -> "UPC-E barcode"
            BarcodeFormat.CODE_39 -> "Code 39"
            BarcodeFormat.CODE_93 -> "Code 93"
            BarcodeFormat.CODE_128 -> "Code 128"
            BarcodeFormat.ITF -> "ITF barcode"
            BarcodeFormat.CODABAR -> "Codabar"
            BarcodeFormat.UNKNOWN -> "Unknown format"
        }
    }

    /**
     * Create detailed description for scan result
     */
    fun getScanResultDescription(
        content: String,
        contentType: ContentType,
        format: BarcodeFormat
    ): String {
        val typeDesc = getContentTypeDescription(contentType)
        val formatDesc = getBarcodeFormatDescription(format)

        return "$typeDesc scanned. Format: $formatDesc. Content: $content"
    }

    /**
     * Create description for history item
     */
    fun getHistoryItemDescription(
        content: String,
        contentType: ContentType,
        isFavorite: Boolean,
        isSelected: Boolean
    ): String {
        val typeDesc = getContentTypeDescription(contentType)
        val favoriteStatus = if (isFavorite) "Favorite" else "Not favorite"
        val selectionStatus = if (isSelected) "Selected" else "Not selected"

        return "$typeDesc: $content. $favoriteStatus. $selectionStatus"
    }

    /**
     * Create description for button action
     */
    fun getActionDescription(action: String, enabled: Boolean = true): String {
        return if (enabled) {
            "$action button"
        } else {
            "$action button, disabled"
        }
    }

    /**
     * Format timestamp for accessibility
     */
    fun formatTimestampForAccessibility(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 604800_000 -> "${diff / 86400_000} days ago"
            else -> "More than a week ago"
        }
    }
}

/**
 * Extension function to add content description to Modifier
 */
fun Modifier.contentDescription(description: String): Modifier {
    return this.semantics {
        contentDescription = description
    }
}

/**
 * Extension function to add state description to Modifier
 */
fun Modifier.stateDescription(description: String): Modifier {
    return this.semantics {
        stateDescription = description
    }
}
