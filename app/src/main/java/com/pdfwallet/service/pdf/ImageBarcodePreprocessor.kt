package com.pdfwallet.service.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object ImageBarcodePreprocessor {
    
    /**
     * Executes the pipeline of fallback strategies, stopping at the first successful read.
     */
    suspend fun tryDecode(originalBitmap: Bitmap, scanner: BarcodeScanner): List<Barcode> {
        
        // Strategy 1: Original image
        originalBitmap.decode(scanner)
            .takeIf { it.isNotEmpty() }
            ?.let { return it }

        // Strategy 2: Grayscale with high contrast threshold
        // Fixes: low contrast, colored backgrounds, subtle watermarks
        val grayscale = grayscaleThreshold(originalBitmap)
        grayscale.decode(scanner)
            .also { grayscale.recycle() }
            .takeIf { it.isNotEmpty() }
            ?.let { return it }

        // Strategy 3: Invert colors
        // Fixes: White QR codes on dark backgrounds (e.g. dark mode screenshots)
        val inverted = invertColors(originalBitmap)
        inverted.decode(scanner)
            .also { inverted.recycle() }
            .takeIf { it.isNotEmpty() }
            ?.let { return it }

        // Strategy 4: Upscale 2x
        // Fixes: thumbnail images, small screenshots
        if (minOf(originalBitmap.width, originalBitmap.height) < 300) {
            val upscaled = upscale(originalBitmap, 2f)
            upscaled.decode(scanner)
                .also { upscaled.recycle() }
                .takeIf { it.isNotEmpty() }
                ?.let { return it }
        }

        // Strategy 5: Crop to center-weighted subregions
        // Fixes: QR in a specific corner; extra content around the barcode
        for (crop in generateSmartCrops(originalBitmap)) {
            val result = crop.decode(scanner)
            crop.recycle()
            if (result.isNotEmpty()) {
                return result
            }
        }

        return emptyList()
    }

    private fun grayscaleThreshold(src: Bitmap): Bitmap {
        val grey = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grey)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        canvas.drawBitmap(src, 0f, 0f, paint)

        // Apply threshold: pixels below 128 luma = black, above = white
        val pixels = IntArray(grey.width * grey.height)
        grey.getPixels(pixels, 0, grey.width, 0, 0, grey.width, grey.height)
        for (i in pixels.indices) {
            val p = pixels[i]
            val luma = (Color.red(p) * 0.299 + Color.green(p) * 0.587 +
                        Color.blue(p) * 0.114).toInt()
            pixels[i] = if (luma < 128) Color.BLACK else Color.WHITE
        }
        grey.setPixels(pixels, 0, grey.width, 0, 0, grey.width, grey.height)
        return grey
    }

    private fun invertColors(src: Bitmap): Bitmap {
        val inverted = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(inverted)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix(floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )))
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return inverted
    }

    private fun upscale(src: Bitmap, factor: Float): Bitmap =
        Bitmap.createScaledBitmap(
            src,
            (src.width * factor).toInt(),
            (src.height * factor).toInt(),
            false  // NEAREST neighbor - preserves sharp QR module edges
        )

    /**
     * Generate smart crop regions: center, each quadrant, top-right, bottom-right.
     * QR codes are most commonly in corners or center of tickets.
     */
    private fun generateSmartCrops(src: Bitmap): List<Bitmap> {
        val w = src.width
        val h = src.height
        val crops = mutableListOf<Bitmap>()

        // Center 60%
        val cx = (w * 0.2).toInt()
        val cy = (h * 0.2).toInt()
        crops.add(Bitmap.createBitmap(src, cx, cy, (w * 0.6).toInt(), (h * 0.6).toInt()))

        // Bottom-right quadrant (common position for IRCTC, PVR tickets)
        crops.add(Bitmap.createBitmap(src, w / 2, h / 2, w / 2, h / 2))

        // Top-right quadrant (common for airline boarding passes)
        crops.add(Bitmap.createBitmap(src, w / 2, 0, w / 2, h / 2))

        // Bottom-left quadrant
        crops.add(Bitmap.createBitmap(src, 0, h / 2, w / 2, h / 2))

        return crops
    }

    private suspend fun Bitmap.decode(scanner: BarcodeScanner): List<Barcode> =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(this, 0)
            scanner.process(image)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(emptyList()) }
        }
}
