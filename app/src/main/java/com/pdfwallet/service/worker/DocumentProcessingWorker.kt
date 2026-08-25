package com.pdfwallet.service.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pdfwallet.data.db.DocumentDao
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.data.db.ProcessingStatus
import com.pdfwallet.service.ai.AiResultMapper
import com.pdfwallet.service.ai.AiResultMapper.MappedResult
import com.pdfwallet.service.ai.GeminiDocumentAnalyser
import com.pdfwallet.service.pdf.DocumentToBitmapConverter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.pdfwallet.util.Logger
import com.pdfwallet.service.pdf.BarcodeExtractor
import com.pdfwallet.service.pdf.ThumbnailGenerator

@HiltWorker
class DocumentProcessingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val documentDao: DocumentDao,
    private val bitmapConverter: DocumentToBitmapConverter,
    private val barcodeExtractor: BarcodeExtractor,
    private val geminiAnalyser: GeminiDocumentAnalyser,
    private val thumbnailGenerator: ThumbnailGenerator,
    private val logger: Logger
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_DOCUMENT_ID = "DOCUMENT_ID"
    }

    override suspend fun doWork(): Result {
        val docId = inputData.getLong(KEY_DOCUMENT_ID, -1)
        if (docId == -1L) {
            logger.e("DocumentProcessingWorker", "Invalid document ID (-1) passed to worker")
            return Result.failure()
        }

        val document = documentDao.getById(docId)
        if (document == null) {
            logger.e("DocumentProcessingWorker", "Document with ID $docId not found in DB")
            return Result.failure()
        }
        documentDao.updateProcessingStatus(docId, ProcessingStatus.PROCESSING)

        var bitmaps = listOf<android.graphics.Bitmap>()
        return try {
            val filePath = document.filePath
            logger.i("DocumentProcessingWorker", "Starting processing for document ID $docId, path: $filePath")

            // ── Step 1: Decode to Bitmaps ───────────────────────────────────────
            bitmaps = bitmapConverter.convert(filePath, maxPages = 3)
            if (bitmaps.isEmpty()) {
                logger.e("DocumentProcessingWorker", "Failed to extract bitmaps from document")
                throw IllegalStateException("Failed to extract bitmaps")
            }

            // ── Step 2: Barcode Extraction (Local) ───────────
            val barcodes = barcodeExtractor.extractBarcodes(bitmaps, document.contentHash)
            logger.d("DocumentProcessingWorker", "Extracted ${barcodes.size} barcodes locally")

            // ── Step 3: Classification & AI Analysis via Gemini ───────────────
            logger.i("DocumentProcessingWorker", "Sending images to Gemini for analysis...")
            val aiResult = geminiAnalyser.analyse(bitmaps)
            val mapped = AiResultMapper.map(aiResult)
            
            logger.i("DocumentProcessingWorker",
                "Final classification: type=${mapped.documentType}, title=${mapped.title}")

            // ── Step 4: Thumbnail ─────────────────────────────────────────────
            val thumbnailPath = thumbnailGenerator.generateThumbnail(filePath)
            logger.d("DocumentProcessingWorker", "Thumbnail generated: $thumbnailPath")

            var mergedDocumentId = mapped.documentId
            if (mergedDocumentId.isNullOrEmpty() && barcodes.isNotEmpty()) {
                mergedDocumentId = barcodes.first()
            }
            
            val finalBarcodes = if (barcodes.isNotEmpty()) barcodes.joinToString(",") else null

            // ── Step 6: Persist to Room ───────────────────────────────────────
            val updatedDoc = document.copy(
                processingStatus    = ProcessingStatus.COMPLETE,
                thumbnailPath       = thumbnailPath,
                rawOcrText          = finalBarcodes, // Store barcodes here instead of OCR text
                title               = mapped.title,
                documentType        = mapped.documentType,
                documentId          = mergedDocumentId,
                holderName          = mapped.holderName,
                issueDate           = mapped.issueDate,
                expiryDate          = mapped.expiryDate,
                expiryDateEpoch     = mapped.expiryDateEpoch,
                sourceLocation      = mapped.sourceLocation,
                destinationLocation = mapped.destinationLocation,
                journeyDate         = mapped.journeyDate,
                bookingStatus       = mapped.bookingStatus,
                additionalMeta      = mapped.ticketMetadata
            )
            documentDao.insert(updatedDoc)

            showNotification(docId, "Added to Wallet ✓", mapped.title)
            logger.i("DocumentProcessingWorker", "Successfully processed document ID $docId")
            Result.success()

        } catch (e: java.io.IOException) {
            logger.e("DocumentProcessingWorker", "Transient I/O error for document ID $docId", e)
            Result.retry()
        } catch (e: OutOfMemoryError) {
            logger.e("DocumentProcessingWorker", "OOM error for document ID $docId, retrying...", e)
            Result.retry()
        } catch (e: Exception) {
            logger.e("DocumentProcessingWorker", "Permanent error processing document ID $docId", e)
            documentDao.updateProcessingStatus(docId, ProcessingStatus.FAILED)
            showNotification(docId, "Processing Failed", "Could not analyse this document")
            Result.failure()
        } finally {
            bitmaps.forEach { it.recycle() }
        }
    }

    private fun showNotification(docId: Long, title: String, content: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return  // Can't post notifications without permission on Android 13+
            }
        }

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(applicationContext, "wallet_processing")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setGroup("WALLET_PROCESSING_GROUP")

        notificationManager.notify(docId.toInt(), builder.build())
        
        val summaryBuilder = NotificationCompat.Builder(applicationContext, "wallet_processing")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setGroup("WALLET_PROCESSING_GROUP")
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            
        notificationManager.notify(100100, summaryBuilder.build())
    }
}
