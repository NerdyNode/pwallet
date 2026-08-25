package com.pdfwallet.service.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThumbnailGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(path, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return android.graphics.BitmapFactory.decodeFile(path, options)
            ?: throw IllegalStateException("Failed to decode image: $path")
    }

    private fun calculateInSampleSize(options: android.graphics.BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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

    suspend fun generateThumbnail(pdfPath: String): String? = withContext(Dispatchers.IO) {
        val file = File(pdfPath)
        if (!file.exists()) return@withContext null
        
        val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(pdfPath, options)
        val isImage = options.outMimeType?.startsWith("image/") == true
        
        if (isImage) {
            val bitmap = decodeSampledBitmap(pdfPath, 400, 560)
            val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, 200, 280, true)
            val cacheDir = File(context.cacheDir, "thumbnails").apply { mkdirs() }
            val thumbFile = File(cacheDir, "${UUID.randomUUID()}.jpg")
            FileOutputStream(thumbFile).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            bitmap.recycle()
            if (scaled != bitmap) scaled.recycle()
            return@withContext thumbFile.absolutePath
        }

        var descriptor: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null

        try {
            descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(descriptor)
            if (renderer.pageCount <= 0) return@withContext null

            page = renderer.openPage(0)
            
            // Render to 200x280 (aspect ratio ~ 1:1.4)
            val bitmap = Bitmap.createBitmap(200, 280, Bitmap.Config.ARGB_8888)
            // Fill with white background
            bitmap.eraseColor(android.graphics.Color.WHITE)
            
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            
            val cacheDir = File(context.cacheDir, "thumbnails").apply { mkdirs() }
            val thumbFile = File(cacheDir, "${UUID.randomUUID()}.jpg")
            
            FileOutputStream(thumbFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            bitmap.recycle()
            
            return@withContext thumbFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            page?.close()
            renderer?.close()
            descriptor?.close()
        }
    }
}
