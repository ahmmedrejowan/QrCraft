package com.rejown.qrcraft.domain.models

data class ScanHistory(
    val id: Long = 0,
    val content: String,
    val rawValue: String,
    val format: BarcodeFormat,
    val contentType: ContentType,
    val timestamp: Long,
    val isFavorite: Boolean = false,
    val metadata: Map<String, String>? = null
)
