package com.rejown.qrcraft.domain.models

data class GeneratedCodeData(
    val id: Long = 0,
    val templateId: String,
    val templateName: String,
    val barcodeFormat: BarcodeFormat,
    val barcodeType: String,
    val title: String? = null,
    val note: String? = null,
    val contentFields: Map<String, String>,
    val formattedContent: String,
    val foregroundColor: Int,
    val backgroundColor: Int,
    val size: Int,
    val errorCorrection: ErrorCorrectionLevel? = null,
    val margin: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean = false,
    val scanCount: Int = 0
)
