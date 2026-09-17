package com.pdfwallet.service.pdf

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val xObjectExtractor: PdfXObjectBarcodeExtractor,
    private val ranker: BarcodeRanker,
) {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )

    suspend fun extract(
        filePath: String,
        bitmapSet: BitmapSet,
    ): RankedBarcode? = withContext(Dispatchers.IO) {

        val results = mutableListOf<RankedBarcode>()

        // Path A: PDF XObject direct extraction
        if (filePath.endsWith(".pdf", ignoreCase = true)) {
            val xObjectResults = xObjectExtractor.extract(filePath)
            if (xObjectResults.isNotEmpty()) {
                results.addAll(xObjectResults.map { it.copy(score = it.score + 20) })
            }
        }

        // Path B: Scan the high-res barcode bitmap (200 DPI render)
        val pageResults = ImageBarcodePreprocessor.tryDecode(bitmapSet.barcodeBitmap, scanner)
        results.addAll(pageResults.map { barcode ->
            // Crop the barcode region from the original bitmap
            val croppedBitmap = barcode.boundingBox?.let { rect ->
                try {
                    val left = maxOf(0, rect.left - 10)
                    val top = maxOf(0, rect.top - 10)
                    val width = minOf(rect.width() + 20, bitmapSet.barcodeBitmap.width - left)
                    val height = minOf(rect.height() + 20, bitmapSet.barcodeBitmap.height - top)
                    if (width > 0 && height > 0) {
                        Bitmap.createBitmap(bitmapSet.barcodeBitmap, left, top, width, height)
                    } else null
                } catch (_: Exception) { null }
            }
            RankedBarcode(
                rawValue = barcode.rawValue ?: "",
                format = barcode.format,
                source = if (filePath.endsWith(".pdf", ignoreCase = true)) RankedBarcode.Source.PAGE_RENDER else RankedBarcode.Source.IMAGE_FILE,
                score = scoreBarcodeFormat(barcode.format),
                bitmap = croppedBitmap
            )
        })

        // Deduplicate by raw value
        val deduplicated = results
            .groupBy { it.rawValue }
            .map { (_, group) -> group.maxByOrNull { it.score }!! }

        // Rank and return
        ranker.rank(deduplicated)
    }

    /**
     * Save the best barcode image to the barcodes directory.
     */
    fun saveBarcodeImage(bitmap: Bitmap, contentHash: String) {
        val barcodesDir = File(context.filesDir, "barcodes").apply { mkdirs() }
        val outFile = File(barcodesDir, "$contentHash.png")
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
