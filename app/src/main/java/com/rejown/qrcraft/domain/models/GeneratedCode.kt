package com.rejown.qrcraft.domain.models

import android.graphics.Bitmap

data class GeneratedCode(
    val content: String,
    val format: BarcodeFormat,
    val contentType: ContentType,
    val bitmap: Bitmap?,
    val customization: CodeCustomization,
    val timestamp: Long = System.currentTimeMillis()
)

data class CodeCustomization(
    val size: Int = 512,
    val foregroundColor: Int = android.graphics.Color.BLACK,
    val backgroundColor: Int = android.graphics.Color.WHITE,
    val errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.MEDIUM,
    val margin: Int = 1
)

enum class ErrorCorrectionLevel(val displayName: String, val zxingLevel: com.google.zxing.qrcode.decoder.ErrorCorrectionLevel) {
    LOW("Low (~7%)", com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L),
    MEDIUM("Medium (~15%)", com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M),
    QUARTILE("Quartile (~25%)", com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q),
    HIGH("High (~30%)", com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H)
}
