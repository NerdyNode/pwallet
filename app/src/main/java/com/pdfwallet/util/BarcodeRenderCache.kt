package com.pdfwallet.util

import android.graphics.Bitmap
import android.util.LruCache
import com.pdfwallet.ui.pass.BarcodeConfig
import com.pdfwallet.ui.pass.BarcodeDisplayFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeRenderCache @Inject constructor() {

    private val cache = LruCache<String, Bitmap>(20)

    suspend fun getBitmap(
        config: BarcodeConfig,
        width: Int = 200,
        height: Int = 200
    ): Bitmap? = withContext(Dispatchers.Default) {
        val key = "${config.format}:${config.value}:${width}x${height}"
        cache.get(key) ?: run {
            try {
                val bitmap = generateBarcode(config, width, height)
                bitmap?.let { cache.put(key, it) }
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun generateBarcode(config: BarcodeConfig, width: Int, height: Int): Bitmap? {
        // Use ZXing BarcodeWriter to generate the barcode
        // This matches the existing BarcodeGenerator pattern in the app
        return try {
            val format = when (config.format) {
                BarcodeDisplayFormat.QR -> com.google.zxing.BarcodeFormat.QR_CODE
                BarcodeDisplayFormat.PDF417 -> com.google.zxing.BarcodeFormat.PDF_417
                BarcodeDisplayFormat.AZTEC -> com.google.zxing.BarcodeFormat.AZTEC
                BarcodeDisplayFormat.CODE_128 -> com.google.zxing.BarcodeFormat.CODE_128
                BarcodeDisplayFormat.CODE_39 -> com.google.zxing.BarcodeFormat.CODE_39
                BarcodeDisplayFormat.UNKNOWN -> com.google.zxing.BarcodeFormat.QR_CODE
            }
            val writer = com.google.zxing.MultiFormatWriter()
            val bitMatrix = writer.encode(config.value, format, width, height)
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix[x, y]) {
                        android.graphics.Color.BLACK
                    } else {
                        android.graphics.Color.WHITE
                    }
                }
            }
            Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            null
        }
    }

    fun evictAll() {
        cache.evictAll()
    }
}
