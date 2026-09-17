package com.pdfwallet.service.pdf

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitOcrExtractor @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Run OCR on a list of page bitmaps and return concatenated text.
     */
    suspend fun extract(bitmaps: List<Bitmap>): String = withContext(Dispatchers.IO) {
        val results = bitmaps.map { bitmap ->
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(image).await().text
            } catch (e: Exception) {
                ""
            }
        }
        results.joinToString("\n\n").trim()
    }
}
