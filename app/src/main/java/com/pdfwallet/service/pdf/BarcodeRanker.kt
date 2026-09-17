package com.pdfwallet.service.pdf

import com.google.mlkit.vision.barcode.common.Barcode
import javax.inject.Inject

class BarcodeRanker @Inject constructor() {

    fun rank(barcodes: List<RankedBarcode>): RankedBarcode? {
        if (barcodes.isEmpty()) return null
        if (barcodes.size == 1) return barcodes.first().copy(score = barcodes.first().score + barcodes.first().sourceBonus())

        return barcodes.map { barcode ->
            barcode.copy(score = barcode.score + barcode.sourceBonus())
        }.maxByOrNull { it.score }
    }
}

fun scoreBarcodeFormat(format: Int): Int = when (format) {
    Barcode.FORMAT_PDF417   -> 100   // IATA boarding pass standard
    Barcode.FORMAT_AZTEC    -> 90    // Common for train tickets
    Barcode.FORMAT_QR_CODE  -> 85    // IRCTC, movie tickets
    Barcode.FORMAT_CODE_128 -> 70    // Generic barcodes
    Barcode.FORMAT_DATA_MATRIX -> 60
    else                    -> 40
}

fun RankedBarcode.sourceBonus(): Int = when (source) {
    RankedBarcode.Source.PDF_XOBJECT -> 20  // Native pixel data, no interpolation
    RankedBarcode.Source.PAGE_RENDER -> 0
    RankedBarcode.Source.IMAGE_FILE  -> 5   // Original photo, full resolution
}
