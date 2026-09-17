package com.pdfwallet.service.pdf

import android.graphics.Bitmap

data class RankedBarcode(
    val rawValue: String,
    val format: Int,
    val source: Source,
    val score: Int = 0,
    val bitmap: Bitmap? = null
) {
    enum class Source {
        PDF_XOBJECT,
        PAGE_RENDER,
        IMAGE_FILE
    }
}
