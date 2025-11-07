package com.rejown.qrcraft.utils.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.ContentType
import com.rejown.qrcraft.domain.models.ScanResult
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ImageDecoder {

    /**
     * Decode QR/Barcode from image using MLKit
     */
    suspend fun decodeWithMLKit(image: InputImage): ScanResult? = suspendCoroutine { continuation ->
        try {
            val scanner = BarcodeScanning.getClient()
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes.first()
                        val rawValue = barcode.rawValue

                        if (rawValue != null) {
                            val displayValue = barcode.displayValue ?: rawValue
                            val format = BarcodeFormat.fromMLKitFormat(barcode.format)
                            val contentType = detectContentType(barcode)

                            continuation.resume(
                                ScanResult(
                                    rawValue = rawValue,
                                    displayValue = displayValue,
                                    format = format,
                                    contentType = contentType
                                )
                            )
                        } else {
                            continuation.resume(null)
                        }
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Failed to decode image with MLKit")
                    continuation.resume(null)
                }
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode image with MLKit")
            continuation.resume(null)
        }
    }

    /**
     * Decode QR/Barcode from image using ZXing (fallback)
     */
    fun decodeWithZXing(bitmap: Bitmap): ScanResult? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val reader = MultiFormatReader()
            val result: Result = reader.decode(binaryBitmap)

            val content = result.text
            val format = mapZXingFormat(result.barcodeFormat)
            val contentType = ContentType.detectFromContent(content)

            ScanResult(
                rawValue = content,
                displayValue = content,
                format = format,
                contentType = contentType
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode image with ZXing")
            null
        }
    }

    private fun detectContentType(barcode: Barcode): ContentType {
        return when (barcode.valueType) {
            Barcode.TYPE_URL -> ContentType.URL
            Barcode.TYPE_EMAIL -> ContentType.EMAIL
            Barcode.TYPE_PHONE -> ContentType.PHONE
            Barcode.TYPE_SMS -> ContentType.SMS
            Barcode.TYPE_WIFI -> ContentType.WIFI
            Barcode.TYPE_CONTACT_INFO -> ContentType.CONTACT
            Barcode.TYPE_CALENDAR_EVENT -> ContentType.CALENDAR
            Barcode.TYPE_GEO -> ContentType.GEO
            Barcode.TYPE_PRODUCT -> ContentType.PRODUCT
            else -> ContentType.detectFromContent(barcode.rawValue ?: "")
        }
    }

    private fun mapZXingFormat(format: com.google.zxing.BarcodeFormat): BarcodeFormat {
        return when (format) {
            com.google.zxing.BarcodeFormat.QR_CODE -> BarcodeFormat.QR_CODE
            com.google.zxing.BarcodeFormat.AZTEC -> BarcodeFormat.AZTEC
            com.google.zxing.BarcodeFormat.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            com.google.zxing.BarcodeFormat.PDF_417 -> BarcodeFormat.PDF417
            com.google.zxing.BarcodeFormat.EAN_8 -> BarcodeFormat.EAN_8
            com.google.zxing.BarcodeFormat.EAN_13 -> BarcodeFormat.EAN_13
            com.google.zxing.BarcodeFormat.UPC_A -> BarcodeFormat.UPC_A
            com.google.zxing.BarcodeFormat.UPC_E -> BarcodeFormat.UPC_E
            com.google.zxing.BarcodeFormat.CODE_39 -> BarcodeFormat.CODE_39
            com.google.zxing.BarcodeFormat.CODE_93 -> BarcodeFormat.CODE_93
            com.google.zxing.BarcodeFormat.CODE_128 -> BarcodeFormat.CODE_128
            com.google.zxing.BarcodeFormat.ITF -> BarcodeFormat.ITF
            com.google.zxing.BarcodeFormat.CODABAR -> BarcodeFormat.CODABAR
            else -> BarcodeFormat.UNKNOWN
        }
    }
}
