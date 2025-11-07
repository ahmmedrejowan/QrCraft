package com.rejown.qrcraft.presentation.scanner.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.ContentType
import com.rejown.qrcraft.domain.models.ScanResult
import timber.log.Timber

class BarcodeAnalyzer(
    private val onBarcodeDetected: (ScanResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private var lastAnalyzedTimestamp = 0L
    private var lastDetectedCode: String? = null
    private var lastDetectedTimestamp = 0L

    companion object {
        private const val ANALYSIS_THROTTLE_MS = 250L // Reduced for better responsiveness
        private const val SCAN_COOLDOWN_MS = 2000L // Prevent duplicate scans within 2 seconds
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()

        // Throttle analysis to avoid excessive processing
        if (currentTimestamp - lastAnalyzedTimestamp < ANALYSIS_THROTTLE_MS) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        lastAnalyzedTimestamp = currentTimestamp
                        processBarcodes(barcodes)
                    }
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Barcode scanning failed")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun processBarcodes(barcodes: List<Barcode>) {
        val barcode = barcodes.firstOrNull() ?: return

        val rawValue = barcode.rawValue ?: return
        val currentTimestamp = System.currentTimeMillis()

        // Prevent duplicate scans of the same code within cooldown period
        if (rawValue == lastDetectedCode &&
            currentTimestamp - lastDetectedTimestamp < SCAN_COOLDOWN_MS) {
            Timber.d("Skipping duplicate scan: $rawValue")
            return
        }

        // Update last detected code and timestamp
        lastDetectedCode = rawValue
        lastDetectedTimestamp = currentTimestamp

        val displayValue = barcode.displayValue ?: rawValue
        val format = BarcodeFormat.fromMLKitFormat(barcode.format)
        val contentType = detectContentType(barcode)
        val metadata = extractMetadata(barcode)

        val scanResult = ScanResult(
            rawValue = rawValue,
            displayValue = displayValue,
            format = format,
            contentType = contentType,
            metadata = metadata
        )

        Timber.d("Barcode detected: format=$format, contentType=$contentType")
        onBarcodeDetected(scanResult)
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

    private fun extractMetadata(barcode: Barcode): Map<String, String>? {
        val metadata = mutableMapOf<String, String>()

        when (barcode.valueType) {
            Barcode.TYPE_WIFI -> {
                barcode.wifi?.let { wifi ->
                    metadata["ssid"] = wifi.ssid ?: ""
                    metadata["password"] = wifi.password ?: ""
                    metadata["encryptionType"] = when (wifi.encryptionType) {
                        Barcode.WiFi.TYPE_OPEN -> "Open"
                        Barcode.WiFi.TYPE_WPA -> "WPA"
                        Barcode.WiFi.TYPE_WEP -> "WEP"
                        else -> "Unknown"
                    }
                }
            }
            Barcode.TYPE_URL -> {
                barcode.url?.let { url ->
                    metadata["url"] = url.url ?: ""
                    metadata["title"] = url.title ?: ""
                }
            }
            Barcode.TYPE_EMAIL -> {
                barcode.email?.let { email ->
                    metadata["address"] = email.address ?: ""
                    metadata["subject"] = email.subject ?: ""
                    metadata["body"] = email.body ?: ""
                }
            }
            Barcode.TYPE_PHONE -> {
                barcode.phone?.let { phone ->
                    metadata["number"] = phone.number ?: ""
                }
            }
            Barcode.TYPE_SMS -> {
                barcode.sms?.let { sms ->
                    metadata["phoneNumber"] = sms.phoneNumber ?: ""
                    metadata["message"] = sms.message ?: ""
                }
            }
            Barcode.TYPE_GEO -> {
                barcode.geoPoint?.let { geo ->
                    metadata["latitude"] = geo.lat.toString()
                    metadata["longitude"] = geo.lng.toString()
                }
            }
            Barcode.TYPE_CONTACT_INFO -> {
                barcode.contactInfo?.let { contact ->
                    metadata["name"] = contact.name?.formattedName ?: ""
                    metadata["organization"] = contact.organization ?: ""
                    contact.phones?.firstOrNull()?.let {
                        metadata["phone"] = it.number ?: ""
                    }
                    contact.emails?.firstOrNull()?.let {
                        metadata["email"] = it.address ?: ""
                    }
                }
            }
        }

        return metadata.ifEmpty { null }
    }
}
