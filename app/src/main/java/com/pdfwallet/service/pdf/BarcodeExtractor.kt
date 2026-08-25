package com.pdfwallet.service.pdf

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileOutputStream

@Singleton
class BarcodeExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_CODE_128
            )
            .build()
    )

    suspend fun extractBarcodes(bitmaps: List<Bitmap>, contentHash: String): List<String> = withContext(Dispatchers.IO) {
        val extractedBarcodes = mutableListOf<String>()
        var barcodeSaved = false
        try {
            val barcodesDir = File(context.filesDir, "barcodes").apply { mkdirs() }
            for (bitmap in bitmaps) {
                val image = InputImage.fromBitmap(bitmap, 0)
                val barcodes = scanner.process(image).await()
                
                for (barcode in barcodes) {
                    barcode.rawValue?.let { extractedBarcodes.add(it) }
                    
                    if (!barcodeSaved && barcode.boundingBox != null) {
                        val box = barcode.boundingBox!!
                        val left = box.left.coerceAtLeast(0)
                        val top = box.top.coerceAtLeast(0)
                        val width = box.width().coerceAtMost(bitmap.width - left)
                        val height = box.height().coerceAtMost(bitmap.height - top)
                        
                        if (width > 0 && height > 0) {
                            val cropped = Bitmap.createBitmap(bitmap, left, top, width, height)
                            val outFile = File(barcodesDir, "$contentHash.png")
                            FileOutputStream(outFile).use { out ->
                                cropped.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            cropped.recycle()
                            barcodeSaved = true
                        }
                    }
                }
            }
            return@withContext extractedBarcodes
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
}
