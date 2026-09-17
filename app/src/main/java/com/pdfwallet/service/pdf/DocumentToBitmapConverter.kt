package com.pdfwallet.service.pdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class BitmapSet(
    val displayBitmap: Bitmap,      // 1920x1080 max - for UI thumbnail + Gemini
    val barcodeBitmap: Bitmap,      // 200 DPI render - for barcode scanning ONLY
)

@Singleton
class DocumentToBitmapConverter @Inject constructor() {

    // Existing constant - unchanged
    private val MAX_DISPLAY_PX = 1920

    // NEW: dedicated barcode render DPI
    // 200 DPI gives ~330px for a QR that's 1 inch on the original page.
    // Enough headroom for ML Kit's ~80px minimum.
    private val BARCODE_DPI = 200

    suspend fun convert(filePath: String): BitmapSet = withContext(Dispatchers.IO) {
        when {
            filePath.endsWith(".pdf", ignoreCase = true) -> convertPdf(filePath)
            else -> convertImage(filePath)
        }
    }

    private fun convertPdf(path: String): BitmapSet {
        val renderer = PdfRenderer(ParcelFileDescriptor.open(
            File(path), ParcelFileDescriptor.MODE_READ_ONLY
        ))
        val page = renderer.openPage(0)

        // Display bitmap - low res, existing logic unchanged
        val displayScale = MAX_DISPLAY_PX.toFloat() / maxOf(page.width, page.height)
        val displayW = (page.width * displayScale).toInt()
        val displayH = (page.height * displayScale).toInt()
        val displayBitmap = Bitmap.createBitmap(displayW, displayH, Bitmap.Config.ARGB_8888)
        displayBitmap.eraseColor(Color.WHITE)
        page.render(displayBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        // Barcode bitmap - 200 DPI dedicated render
        // A4 page is 595pt x 842pt (PDF points = 1/72 inch)
        // At 200 DPI: 595 * (200/72) = 1653px width
        val dpiScale = BARCODE_DPI / 72f
        val barcodeW = (page.width * dpiScale).toInt()
        val barcodeH = (page.height * dpiScale).toInt()
        val barcodeBitmap = Bitmap.createBitmap(barcodeW, barcodeH, Bitmap.Config.ARGB_8888)
        barcodeBitmap.eraseColor(Color.WHITE)
        page.render(barcodeBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        page.close()
        renderer.close()

        return BitmapSet(displayBitmap, barcodeBitmap)
    }

    private fun convertImage(path: String): BitmapSet {
        val original = BitmapFactory.decodeFile(path)
            ?: throw IllegalStateException("Cannot decode image: $path")

        // Display bitmap - scale down as before
        val displayBitmap = scaleBitmap(original, MAX_DISPLAY_PX)

        // For images, use the original full-res for barcode scanning
        // (no rendering step needed - it's already pixel data)
        val barcodeBitmap = original  // full resolution

        return BitmapSet(displayBitmap, barcodeBitmap)
    }

    private fun scaleBitmap(src: Bitmap, maxPx: Int): Bitmap {
        val scale = minOf(1f, maxPx.toFloat() / maxOf(src.width, src.height))
        if (scale == 1f) return src
        return Bitmap.createScaledBitmap(
            src,
            (src.width * scale).toInt(),
            (src.height * scale).toInt(),
            true
        )
    }
}
