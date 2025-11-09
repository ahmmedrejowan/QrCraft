package com.rejown.qrcraft.domain.models

enum class BarcodeFormat(
    val displayName: String,
    val type: BarcodeType
) {
    // 2D Barcodes
    QR_CODE("QR Code", BarcodeType.TWO_D),
    AZTEC("Aztec", BarcodeType.TWO_D),
    DATA_MATRIX("Data Matrix", BarcodeType.TWO_D),
    PDF417("PDF417", BarcodeType.TWO_D),

    // 1D Barcodes
    EAN_8("EAN-8", BarcodeType.ONE_D),
    EAN_13("EAN-13", BarcodeType.ONE_D),
    UPC_A("UPC-A", BarcodeType.ONE_D),
    UPC_E("UPC-E", BarcodeType.ONE_D),
    CODE_39("Code 39", BarcodeType.ONE_D),
    CODE_93("Code 93", BarcodeType.ONE_D),
    CODE_128("Code 128", BarcodeType.ONE_D),
    ITF("ITF", BarcodeType.ONE_D),
    CODABAR("Codabar", BarcodeType.ONE_D),
    UNKNOWN("Unknown", BarcodeType.TWO_D);

    fun toZXingFormat(): com.google.zxing.BarcodeFormat {
        return when (this) {
            QR_CODE -> com.google.zxing.BarcodeFormat.QR_CODE
            AZTEC -> com.google.zxing.BarcodeFormat.AZTEC
            DATA_MATRIX -> com.google.zxing.BarcodeFormat.DATA_MATRIX
            PDF417 -> com.google.zxing.BarcodeFormat.PDF_417
            EAN_8 -> com.google.zxing.BarcodeFormat.EAN_8
            EAN_13 -> com.google.zxing.BarcodeFormat.EAN_13
            UPC_A -> com.google.zxing.BarcodeFormat.UPC_A
            UPC_E -> com.google.zxing.BarcodeFormat.UPC_E
            CODE_39 -> com.google.zxing.BarcodeFormat.CODE_39
            CODE_93 -> com.google.zxing.BarcodeFormat.CODE_93
            CODE_128 -> com.google.zxing.BarcodeFormat.CODE_128
            ITF -> com.google.zxing.BarcodeFormat.ITF
            CODABAR -> com.google.zxing.BarcodeFormat.CODABAR
            UNKNOWN -> com.google.zxing.BarcodeFormat.QR_CODE // Default to QR
        }
    }

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

        fun getByType(type: BarcodeType): List<BarcodeFormat> {
            return values().filter { it.type == type && it != UNKNOWN }
        }
    }
}

enum class BarcodeType(val displayName: String) {
    ONE_D("1D Barcode"),
    TWO_D("2D Barcode")
}
