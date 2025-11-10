package com.rejown.qrcraft.utils

import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.ErrorCorrectionLevel

/**
 * Utility class for calculating QR code and barcode capacity limits
 * Based on official QR Code specifications and ZXing library limitations
 */
object QRCapacityCalculator {

    /**
     * Encoding modes for QR codes
     */
    enum class EncodingMode {
        NUMERIC,       // 0-9
        ALPHANUMERIC,  // 0-9, A-Z, space, $, %, *, +, -, ., /, :
        BYTE,          // ISO-8859-1 or UTF-8
        KANJI          // Shift JIS Kanji
    }

    /**
     * QR Code capacity limits based on error correction level and encoding mode
     * These are maximum capacities for Version 40 (177x177 modules) QR codes
     */
    private val QR_CODE_LIMITS = mapOf(
        ErrorCorrectionLevel.LOW to mapOf(
            EncodingMode.NUMERIC to 7089,
            EncodingMode.ALPHANUMERIC to 4296,
            EncodingMode.BYTE to 2953,
            EncodingMode.KANJI to 1817
        ),
        ErrorCorrectionLevel.MEDIUM to mapOf(
            EncodingMode.NUMERIC to 5596,
            EncodingMode.ALPHANUMERIC to 3391,
            EncodingMode.BYTE to 2331,
            EncodingMode.KANJI to 1435
        ),
        ErrorCorrectionLevel.QUARTILE to mapOf(
            EncodingMode.NUMERIC to 3993,
            EncodingMode.ALPHANUMERIC to 2420,
            EncodingMode.BYTE to 1663,
            EncodingMode.KANJI to 1024
        ),
        ErrorCorrectionLevel.HIGH to mapOf(
            EncodingMode.NUMERIC to 3057,
            EncodingMode.ALPHANUMERIC to 1852,
            EncodingMode.BYTE to 1273,
            EncodingMode.KANJI to 784
        )
    )

    /**
     * Fixed capacity limits for 1D barcodes
     */
    private val ONE_D_LIMITS = mapOf(
        BarcodeFormat.EAN_8 to 7,      // 7 digits + 1 check digit
        BarcodeFormat.EAN_13 to 12,    // 12 digits + 1 check digit
        BarcodeFormat.UPC_A to 11,     // 11 digits + 1 check digit
        BarcodeFormat.UPC_E to 6,      // 6 digits + 1 check digit
        BarcodeFormat.CODE_39 to 43,   // Practical limit, technically unlimited
        BarcodeFormat.CODE_93 to 47,   // Practical limit
        BarcodeFormat.CODE_128 to 80,  // Practical limit
        BarcodeFormat.ITF to 80,       // Practical limit, even number of digits
        BarcodeFormat.CODABAR to 40    // Practical limit
    )

    /**
     * 2D barcode capacities (approximate maximums)
     */
    private val TWO_D_LIMITS = mapOf(
        BarcodeFormat.DATA_MATRIX to mapOf(
            EncodingMode.NUMERIC to 3116,
            EncodingMode.ALPHANUMERIC to 2335,
            EncodingMode.BYTE to 1556
        ),
        BarcodeFormat.AZTEC to mapOf(
            EncodingMode.NUMERIC to 3832,
            EncodingMode.ALPHANUMERIC to 3067,
            EncodingMode.BYTE to 1914
        ),
        BarcodeFormat.PDF417 to mapOf(
            EncodingMode.NUMERIC to 2710,
            EncodingMode.ALPHANUMERIC to 1850,
            EncodingMode.BYTE to 1108
        )
    )

    /**
     * Detect encoding mode from content
     */
    fun detectEncodingMode(content: String): EncodingMode {
        if (content.isEmpty()) return EncodingMode.BYTE

        // Check if numeric only
        if (content.all { it.isDigit() }) {
            return EncodingMode.NUMERIC
        }

        // Check if alphanumeric only (QR alphanumeric charset)
        val alphanumericChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"
        if (content.uppercase().all { it in alphanumericChars }) {
            return EncodingMode.ALPHANUMERIC
        }

        // Default to byte mode
        return EncodingMode.BYTE
    }

    /**
     * Get maximum capacity for a specific barcode format
     */
    fun getMaxCapacity(
        format: BarcodeFormat,
        errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.MEDIUM,
        content: String = ""
    ): Int {
        return when (format) {
            BarcodeFormat.QR_CODE -> {
                val mode = detectEncodingMode(content)
                QR_CODE_LIMITS[errorCorrection]?.get(mode) ?: 2331 // Default to MEDIUM/BYTE
            }
            BarcodeFormat.DATA_MATRIX, BarcodeFormat.AZTEC, BarcodeFormat.PDF417 -> {
                val mode = detectEncodingMode(content)
                TWO_D_LIMITS[format]?.get(mode) ?: 1000 // Safe default
            }
            else -> {
                // 1D barcodes
                ONE_D_LIMITS[format] ?: 80 // Safe default
            }
        }
    }

    /**
     * Check if content exceeds capacity
     */
    fun isWithinCapacity(
        content: String,
        format: BarcodeFormat,
        errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.MEDIUM
    ): Boolean {
        val maxCapacity = getMaxCapacity(format, errorCorrection, content)
        return content.length <= maxCapacity
    }

    /**
     * Get remaining capacity
     */
    fun getRemainingCapacity(
        content: String,
        format: BarcodeFormat,
        errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.MEDIUM
    ): Int {
        val maxCapacity = getMaxCapacity(format, errorCorrection, content)
        return maxCapacity - content.length
    }

    /**
     * Get capacity percentage used
     */
    fun getCapacityPercentage(
        content: String,
        format: BarcodeFormat,
        errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.MEDIUM
    ): Float {
        val maxCapacity = getMaxCapacity(format, errorCorrection, content)
        if (maxCapacity == 0) return 0f
        return (content.length.toFloat() / maxCapacity.toFloat() * 100f).coerceIn(0f, 100f)
    }

    /**
     * Get user-friendly capacity message
     */
    fun getCapacityMessage(
        currentLength: Int,
        format: BarcodeFormat,
        errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.MEDIUM,
        content: String = ""
    ): String {
        val maxCapacity = getMaxCapacity(format, errorCorrection, content)
        val remaining = maxCapacity - currentLength

        return when {
            remaining < 0 -> "Exceeds limit by ${-remaining} characters"
            remaining == 0 -> "At maximum capacity"
            remaining < 10 -> "Only $remaining characters remaining"
            else -> "$currentLength / $maxCapacity characters"
        }
    }

    /**
     * Validate content for specific barcode formats with special requirements
     */
    fun validateFormatSpecificContent(content: String, format: BarcodeFormat): String? {
        return when (format) {
            BarcodeFormat.EAN_8 -> {
                if (!content.all { it.isDigit() }) "EAN-8 requires only digits"
                else if (content.length != 7 && content.length != 8) "EAN-8 requires exactly 7 or 8 digits"
                else null
            }
            BarcodeFormat.EAN_13 -> {
                if (!content.all { it.isDigit() }) "EAN-13 requires only digits"
                else if (content.length != 12 && content.length != 13) "EAN-13 requires exactly 12 or 13 digits"
                else null
            }
            BarcodeFormat.UPC_A -> {
                if (!content.all { it.isDigit() }) "UPC-A requires only digits"
                else if (content.length != 11 && content.length != 12) "UPC-A requires exactly 11 or 12 digits"
                else null
            }
            BarcodeFormat.UPC_E -> {
                if (!content.all { it.isDigit() }) "UPC-E requires only digits"
                else if (content.length != 6 && content.length != 7 && content.length != 8) "UPC-E requires 6, 7, or 8 digits"
                else null
            }
            BarcodeFormat.ITF -> {
                if (!content.all { it.isDigit() }) "ITF requires only digits"
                else if (content.length % 2 != 0) "ITF requires an even number of digits"
                else null
            }
            BarcodeFormat.CODE_39 -> {
                // Code 39 supports: 0-9, A-Z, -, ., space, *, $, /, +, %
                val validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%"
                if (!content.uppercase().all { it in validChars })
                    "Code 39 only supports: 0-9, A-Z, space, and -.*$/+%"
                else null
            }
            else -> null // No specific validation for other formats
        }
    }
}
