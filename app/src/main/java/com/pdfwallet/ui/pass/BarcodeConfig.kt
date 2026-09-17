package com.pdfwallet.ui.pass

data class BarcodeConfig(
    val format: BarcodeDisplayFormat,
    val value: String,
    val altText: String? = null
)

enum class BarcodeDisplayFormat {
    QR, PDF417, AZTEC, CODE_128, CODE_39, UNKNOWN
}
