package com.rejown.qrcraft.utils.generator

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.rejown.qrcraft.domain.models.CodeCustomization
import timber.log.Timber

object CodeGenerator {

    fun generateQRCode(
        content: String,
        customization: CodeCustomization = CodeCustomization()
    ): Bitmap? {
        return generateCode(
            content = content,
            format = BarcodeFormat.QR_CODE,
            customization = customization
        )
    }

    fun generateCode(
        content: String,
        format: com.rejown.qrcraft.domain.models.BarcodeFormat,
        customization: CodeCustomization = CodeCustomization()
    ): Bitmap? {
        val zxingFormat = mapToZXingFormat(format)
        return generateCode(content, zxingFormat, customization)
    }

    private fun generateCode(
        content: String,
        format: BarcodeFormat,
        customization: CodeCustomization = CodeCustomization()
    ): Bitmap? {
        if (content.isEmpty()) {
            Timber.w("Cannot generate code with empty content")
            return null
        }

        return try {
            val hints = mutableMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = customization.margin

            // Error correction only applies to QR codes
            if (format == BarcodeFormat.QR_CODE) {
                hints[EncodeHintType.ERROR_CORRECTION] = when (customization.errorCorrectionLevel) {
                    com.rejown.qrcraft.domain.models.ErrorCorrectionLevel.LOW -> ErrorCorrectionLevel.L
                    com.rejown.qrcraft.domain.models.ErrorCorrectionLevel.MEDIUM -> ErrorCorrectionLevel.M
                    com.rejown.qrcraft.domain.models.ErrorCorrectionLevel.QUARTILE -> ErrorCorrectionLevel.Q
                    com.rejown.qrcraft.domain.models.ErrorCorrectionLevel.HIGH -> ErrorCorrectionLevel.H
                }
            }

            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(
                content,
                format,
                customization.size,
                customization.size,
                hints
            )

            createBitmap(bitMatrix, customization)
        } catch (e: WriterException) {
            Timber.e(e, "Failed to generate code")
            null
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during code generation")
            null
        }
    }

    private fun createBitmap(
        bitMatrix: BitMatrix,
        customization: CodeCustomization
    ): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) {
                    customization.foregroundColor
                } else {
                    customization.backgroundColor
                }
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun formatContentForType(
        content: String,
        contentType: com.rejown.qrcraft.domain.models.ContentType
    ): String {
        return when (contentType) {
            com.rejown.qrcraft.domain.models.ContentType.URL -> {
                if (!content.startsWith("http://") && !content.startsWith("https://")) {
                    "https://$content"
                } else {
                    content
                }
            }
            com.rejown.qrcraft.domain.models.ContentType.EMAIL -> {
                if (!content.startsWith("mailto:")) {
                    "mailto:$content"
                } else {
                    content
                }
            }
            com.rejown.qrcraft.domain.models.ContentType.PHONE -> {
                if (!content.startsWith("tel:")) {
                    "tel:$content"
                } else {
                    content
                }
            }
            com.rejown.qrcraft.domain.models.ContentType.SMS -> {
                if (!content.startsWith("smsto:")) {
                    "smsto:$content"
                } else {
                    content
                }
            }
            else -> content
        }
    }

    fun createWiFiString(ssid: String, password: String, encryption: String = "WPA"): String {
        return "WIFI:T:$encryption;S:$ssid;P:$password;;"
    }

    fun createVCard(
        name: String,
        phone: String? = null,
        email: String? = null,
        organization: String? = null,
        website: String? = null
    ): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:$name")
            phone?.let { appendLine("TEL:$it") }
            email?.let { appendLine("EMAIL:$it") }
            organization?.let { appendLine("ORG:$it") }
            website?.let { appendLine("URL:$it") }
            append("END:VCARD")
        }
    }

    fun createGeoLocation(latitude: Double, longitude: Double): String {
        return "geo:$latitude,$longitude"
    }

    private fun mapToZXingFormat(format: com.rejown.qrcraft.domain.models.BarcodeFormat): BarcodeFormat {
        return when (format) {
            com.rejown.qrcraft.domain.models.BarcodeFormat.QR_CODE -> BarcodeFormat.QR_CODE
            com.rejown.qrcraft.domain.models.BarcodeFormat.AZTEC -> BarcodeFormat.AZTEC
            com.rejown.qrcraft.domain.models.BarcodeFormat.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            com.rejown.qrcraft.domain.models.BarcodeFormat.PDF417 -> BarcodeFormat.PDF_417
            com.rejown.qrcraft.domain.models.BarcodeFormat.EAN_8 -> BarcodeFormat.EAN_8
            com.rejown.qrcraft.domain.models.BarcodeFormat.EAN_13 -> BarcodeFormat.EAN_13
            com.rejown.qrcraft.domain.models.BarcodeFormat.UPC_A -> BarcodeFormat.UPC_A
            com.rejown.qrcraft.domain.models.BarcodeFormat.UPC_E -> BarcodeFormat.UPC_E
            com.rejown.qrcraft.domain.models.BarcodeFormat.CODE_39 -> BarcodeFormat.CODE_39
            com.rejown.qrcraft.domain.models.BarcodeFormat.CODE_93 -> BarcodeFormat.CODE_93
            com.rejown.qrcraft.domain.models.BarcodeFormat.CODE_128 -> BarcodeFormat.CODE_128
            com.rejown.qrcraft.domain.models.BarcodeFormat.ITF -> BarcodeFormat.ITF
            com.rejown.qrcraft.domain.models.BarcodeFormat.CODABAR -> BarcodeFormat.CODABAR
            com.rejown.qrcraft.domain.models.BarcodeFormat.UNKNOWN -> BarcodeFormat.QR_CODE
        }
    }
}
