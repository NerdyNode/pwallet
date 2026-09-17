package com.pdfwallet.service.pdf

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class PdfXObjectBarcodeExtractor @Inject constructor() {
    private val barcodeScanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient(
        com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS)
            .build()
    )
    data class XObjectCandidate(
        val name: String,
        val bitmap: Bitmap,
        val width: Int,
        val height: Int,
        val isColor: Boolean,
    )

    suspend fun extract(filePath: String): List<RankedBarcode> = withContext(Dispatchers.IO) {
        val candidates = mutableListOf<XObjectCandidate>()
        val seenSizes = mutableSetOf<Pair<Int,Int>>()

        try {
            val document = PDDocument.load(File(filePath))
            for (page in document.pages) {
                val resources = page.resources ?: continue
                for (xObjectName in resources.xObjectNames) {
                    val xObject = resources.getXObject(xObjectName)
                    if (xObject is PDImageXObject) {
                        val width = xObject.width
                        val height = xObject.height
                        
                        // 1. Skip images that are too small
                        if (minOf(width, height) < 80) continue

                        val colorSpace = xObject.colorSpace
                        val isColor = colorSpace.name != "DeviceGray"

                        // 2. Skip if we already processed same dimensions (color/grey duplicate)
                        val sizeKey = Pair(width, height)
                        if (sizeKey in seenSizes && !isColor) continue
                        seenSizes.add(sizeKey)

                        // 3. Create bitmap
                        val bitmap = try {
                            xObject.image
                        } catch (e: Exception) {
                            null
                        } ?: continue

                        // 4. Skip blank bitmaps (IRCTC grayscale QR bug)
                        if (BarcodePreFilter.isBlank(bitmap)) {
                            bitmap.recycle()
                            continue
                        }

                        // 5. General pre-filter
                        if (!BarcodePreFilter.shouldAttemptDecode(bitmap)) {
                            bitmap.recycle()
                            continue
                        }

                        candidates.add(XObjectCandidate(xObjectName.name, bitmap, width, height, isColor))
                    }
                }
            }
            document.close()
        } catch (e: Exception) {
            return@withContext emptyList()
        }

        val results = mutableListOf<RankedBarcode>()
        for (candidate in candidates.sortedByDescending { it.isColor }) {
            try {
                val image = InputImage.fromBitmap(candidate.bitmap, 0)
                val barcodes = barcodeScanner.process(image).await()
                if (barcodes.isNotEmpty()) {
                    // Use the first barcode and keep the bitmap for saving to disk
                    val barcode = barcodes.first()
                    results.add(
                        RankedBarcode(
                            rawValue = barcode.rawValue ?: "",
                            format = barcode.format,
                            source = RankedBarcode.Source.PDF_XOBJECT,
                            score = scoreBarcodeFormat(barcode.format),
                            bitmap = candidate.bitmap // Preserve original bitmap
                        )
                    )
                    // Add remaining barcodes without bitmap
                    barcodes.drop(1).forEach { extraBarcode ->
                        results.add(
                            RankedBarcode(
                                rawValue = extraBarcode.rawValue ?: "",
                                format = extraBarcode.format,
                                source = RankedBarcode.Source.PDF_XOBJECT,
                                score = scoreBarcodeFormat(extraBarcode.format)
                            )
                        )
                    }
                } else {
                    candidate.bitmap.recycle()
                }
            } catch (e: Exception) {
                candidate.bitmap.recycle()
            }
        }

        results
    }
}
