package com.rejown.qrcraft.domain.models

data class ScanResult(
    val rawValue: String,
    val displayValue: String,
    val format: BarcodeFormat,
    val contentType: ContentType,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String>? = null
)
