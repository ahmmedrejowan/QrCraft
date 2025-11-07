package com.rejown.qrcraft.domain.models

enum class BarcodeFormat(val displayName: String) {
    QR_CODE("QR Code"),
    AZTEC("Aztec"),
    DATA_MATRIX("Data Matrix"),
    PDF417("PDF417"),
    EAN_8("EAN-8"),
    EAN_13("EAN-13"),
    UPC_A("UPC-A"),
    UPC_E("UPC-E"),
    CODE_39("Code 39"),
    CODE_93("Code 93"),
    CODE_128("Code 128"),
    ITF("ITF"),
    CODABAR("Codabar"),
    UNKNOWN("Unknown");

    companion object {
        fun fromMLKitFormat(mlkitFormat: Int): BarcodeFormat {
            return when (mlkitFormat) {
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE -> QR_CODE
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC -> AZTEC
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX -> DATA_MATRIX
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417 -> PDF417
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> EAN_8
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> EAN_13
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> UPC_A
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> UPC_E
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39 -> CODE_39
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93 -> CODE_93
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> CODE_128
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF -> ITF
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR -> CODABAR
                else -> UNKNOWN
            }
        }
    }
}
