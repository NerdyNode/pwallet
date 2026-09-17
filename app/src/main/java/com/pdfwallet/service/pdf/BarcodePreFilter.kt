package com.pdfwallet.service.pdf

import android.graphics.Bitmap
import android.graphics.Color

object BarcodePreFilter {

    /**
     * Determines if a bitmap is worth sending to ML Kit for barcode scanning.
     * Rejects obvious non-barcodes to save processing time and avoid false positives.
     */
    fun shouldAttemptDecode(bitmap: Bitmap, pageArea: Int? = null): Boolean {

        // 1. Minimum dimensions - ML Kit needs at least ~80px
        if (bitmap.width < 50 || bitmap.height < 50) {
            return false
        }

        // 2. Aspect Ratio - Barcodes are generally square (QR) or wide rectangles
        // Tall, thin images (like vertical banners) are never barcodes
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        if (aspectRatio < 0.2f || aspectRatio > 15f) {
            return false
        }

        // 3. Page Area Heuristic (for PDF XObjects)
        // A barcode is rarely more than 20% of the entire page.
        // If an image is 95% of the page, it's a background image or full page scan.
        if (pageArea != null) {
            val imageArea = bitmap.width * bitmap.height
            if (imageArea > (pageArea * 0.5f)) {
                return false
            }
        }

        // 4. Contrast Heuristic
        // Barcodes require high contrast (usually black and white).
        // A photograph or low-contrast decorative image will fail this.
        return hasHighContrastContent(bitmap)
    }

    /**
     * Checks if the image has sufficient dark and light pixels.
     * Takes a fast sparse sampling of pixels (e.g., every 5th pixel on a grid)
     */
    private fun hasHighContrastContent(bitmap: Bitmap): Boolean {
        var darkPixels = 0
        var lightPixels = 0
        var totalSamples = 0

        val stepX = (bitmap.width / 20).coerceAtLeast(1)
        val stepY = (bitmap.height / 20).coerceAtLeast(1)

        for (x in 0 until bitmap.width step stepX) {
            for (y in 0 until bitmap.height step stepY) {
                val pixel = bitmap.getPixel(x, y)
                val luma = (Color.red(pixel) * 0.299 +
                            Color.green(pixel) * 0.587 +
                            Color.blue(pixel) * 0.114).toInt()

                if (luma < 80) darkPixels++
                if (luma > 180) lightPixels++
                totalSamples++
            }
        }

        // Requires at least 5% of sampled pixels to be distinctly dark
        // and 5% to be distinctly light
        val minRequired = (totalSamples * 0.05).toInt()
        return darkPixels > minRequired && lightPixels > minRequired
    }

    /**
     * IRCTC PDFs specifically contain a completely blank grayscale copy of the QR code
     * that sits on top of the real RGB one. If we scan it, ML Kit sees nothing.
     */
    fun isBlank(bitmap: Bitmap): Boolean {
        val stepX = (bitmap.width / 10).coerceAtLeast(1)
        val stepY = (bitmap.height / 10).coerceAtLeast(1)

        val firstPixel = bitmap.getPixel(0, 0)
        
        for (x in 0 until bitmap.width step stepX) {
            for (y in 0 until bitmap.height step stepY) {
                if (bitmap.getPixel(x, y) != firstPixel) {
                    return false // Found at least one differing pixel, not blank
                }
            }
        }
        return true
    }
}
