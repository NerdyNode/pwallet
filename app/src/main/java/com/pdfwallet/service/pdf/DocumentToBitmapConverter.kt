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

@Singleton
class DocumentToBitmapConverter @Inject constructor() {
    
    private fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
            ?: throw IllegalStateException("Failed to decode image: $path")
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfH = height / 2
            val halfW = width / 2
            while (halfH / inSampleSize >= reqHeight && halfW / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    suspend fun convert(filePath: String, maxPages: Int = 3): List<Bitmap> = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) return@withContext emptyList()
        
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(filePath, options)
        val isImage = options.outMimeType?.startsWith("image/") == true
        
        if (isImage) {
            val bitmap = decodeSampledBitmap(filePath, 1920, 1080)
            return@withContext listOf(bitmap)
        }

        var descriptor: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        val bitmaps = mutableListOf<Bitmap>()

        try {
            descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(descriptor)
            if (renderer.pageCount <= 0) return@withContext emptyList()

            val pagesToProcess = java.lang.Math.min(renderer.pageCount, maxPages)
            for (i in 0 until pagesToProcess) {
                var page: PdfRenderer.Page? = null
                try {
                    page = renderer.openPage(i)
                    val density = 1.5f
                    var targetWidth = (page.width * density).toInt()
                    var targetHeight = (page.height * density).toInt()
                    // Cap dimensions to avoid OOM
                    val maxDim = 2048
                    if (targetWidth > maxDim || targetHeight > maxDim) {
                        val scale = maxDim.toFloat() / Math.max(targetWidth, targetHeight)
                        targetWidth = (targetWidth * scale).toInt()
                        targetHeight = (targetHeight * scale).toInt()
                    }
                    val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    page?.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            renderer?.close()
            descriptor?.close()
        }
        
        return@withContext bitmaps
    }
}
