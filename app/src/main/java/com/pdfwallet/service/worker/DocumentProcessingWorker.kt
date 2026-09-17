package com.pdfwallet.service.worker

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pdfwallet.data.db.DocumentDao
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.data.db.ProcessingStatus
import com.pdfwallet.service.ai.AiResultMapper
import com.pdfwallet.service.ai.ExtractionValidator
import com.pdfwallet.service.ai.GeminiDocumentAnalyser
import com.pdfwallet.service.pdf.BarcodeExtractor
import com.pdfwallet.service.pdf.BarcodeRanker
import com.pdfwallet.service.pdf.DocumentToBitmapConverter
import com.pdfwallet.service.pdf.MlKitOcrExtractor
import com.pdfwallet.service.pdf.PdfBoxTextExtractor
import com.pdfwallet.service.pdf.TextMerger
import com.pdfwallet.service.pdf.TextPreProcessor
import com.pdfwallet.service.pdf.TextQualityChecker
import com.pdfwallet.service.pdf.ThumbnailGenerator
import com.pdfwallet.util.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@HiltWorker
class DocumentProcessingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val documentDao: DocumentDao,
    private val bitmapConverter: DocumentToBitmapConverter,
    private val barcodeExtractor: BarcodeExtractor,
    private val geminiAnalyser: GeminiDocumentAnalyser,
    private val thumbnailGenerator: ThumbnailGenerator,
    private val pdfBoxExtractor: PdfBoxTextExtractor,
    private val ocrExtractor: MlKitOcrExtractor,
    private val logger: Logger
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_DOCUMENT_ID = "DOCUMENT_ID"
        private const val TAG = "DocumentProcessingWorker"
    }

    override suspend fun doWork(): Result {
        val docId = inputData.getLong(KEY_DOCUMENT_ID, -1)
        if (docId == -1L) {
            logger.e(TAG, "Invalid document ID (-1) passed to worker")
            return Result.failure()
        }

        val document = documentDao.getById(docId)
        if (document == null) {
            logger.e(TAG, "Document with ID $docId not found in DB")
            return Result.failure()
        }
        documentDao.updateProcessingStatus(docId, ProcessingStatus.PROCESSING)

        var bitmapSet: com.pdfwallet.service.pdf.BitmapSet? = null
        var bitmaps = listOf<Bitmap>()
        return try {
            val filePath = document.filePath
            logger.i(TAG, "Starting pipeline for document ID $docId, path: $filePath")

            // ── Step 1: Decode to Bitmaps ────────────────────────────────────
            bitmapSet = bitmapConverter.convert(filePath)
            bitmaps = listOf(bitmapSet.displayBitmap)
            if (bitmaps.isEmpty()) {
                logger.e(TAG, "Failed to extract bitmaps from document")
                throw IllegalStateException("Failed to extract bitmaps")
            }

            // ── Step 2: Parallel Text Extraction ─────────────────────────────
            val extractedText = coroutineScope {
                val pdfBoxDeferred = async(Dispatchers.IO) {
                    try {
                        val text = pdfBoxExtractor.extract(filePath)
                        TextMerger.ExtractionResult(text, TextMerger.Source.PDFBOX, 0.9f)
                            .takeIf { text.isNotBlank() }
                    } catch (e: Exception) {
                        logger.w(TAG, "PdfBox extraction failed: ${e.message}")
                        null
                    }
                }

                val ocrDeferred = async(Dispatchers.IO) {
                    try {
                        val text = ocrExtractor.extract(bitmaps)
                        TextMerger.ExtractionResult(text, TextMerger.Source.OCR, 0.8f)
                            .takeIf { text.isNotBlank() }
                    } catch (e: Exception) {
                        logger.w(TAG, "OCR extraction failed: ${e.message}")
                        null
                    }
                }

                TextMerger.merge(pdfBoxDeferred.await(), ocrDeferred.await())
            }
            logger.d(TAG, "Text extraction complete: source=${extractedText.source}, " +
                "confidence=${extractedText.confidence}, length=${extractedText.text.length}")

            // ── Step 3: Quality Gate ─────────────────────────────────────────
            val qualityReport = TextQualityChecker.assess(extractedText.text)
            logger.d(TAG, "Quality: ${qualityReport.quality}, words=${qualityReport.wordCount}, " +
                "garbled=${qualityReport.garbledRatio}")

            val textToProcess = when (qualityReport.quality) {
                TextQualityChecker.Quality.POOR -> {
                    logger.w(TAG, "Text quality POOR — falling back to bitmap-only Gemini")
                    // Fall back to original bitmap-based analysis
                    val aiResult = geminiAnalyser.analyse(bitmaps)
                    return finishWithAiResult(docId, document, aiResult, bitmaps, ProcessingStatus.COMPLETE)
                }
                TextQualityChecker.Quality.LOW -> {
                    logger.d(TAG, "Text quality LOW — using available text anyway")
                    extractedText.text
                }
                TextQualityChecker.Quality.GOOD -> extractedText.text
            }

            // Step 4: Barcode Extraction (parallel with text processing)
            val bestBarcode = bitmapSet?.let { barcodeExtractor.extract(filePath, it) }
            logger.d(TAG, "Barcode extraction: best=${bestBarcode?.rawValue?.take(20)}")

            // ── Step 5: Text Pre-Processing ──────────────────────────────────
            val suspectedType = TextPreProcessor.preclassify(textToProcess)
            val cleanedText = TextPreProcessor.clean(textToProcess)
            val structuredText = TextPreProcessor.structure(cleanedText, suspectedType)
            logger.d(TAG, "Pre-classified type: $suspectedType")

            // ── Step 6: Hybrid Gemini Analysis (text + first page image) ─────
            logger.i(TAG, "Sending hybrid input to Gemini (text + image)...")
            val aiResult = geminiAnalyser.analyseHybrid(
                structuredText = structuredText,
                firstPageBitmap = bitmaps.firstOrNull(),
                suspectedType = suspectedType
            )

            // ── Step 7: Output Validation ────────────────────────────────────
            val mapped = AiResultMapper.map(aiResult)
            val extractedFields = buildFieldMap(mapped)
            val finalType = suspectedType ?: mapped.documentType
            val validationResult = ExtractionValidator.validate(extractedFields, finalType)

            val status = when (validationResult) {
                is ExtractionValidator.ValidationResult.OK -> {
                    logger.i(TAG, "Validation: OK")
                    ProcessingStatus.COMPLETE
                }
                is ExtractionValidator.ValidationResult.NeedsReview -> {
                    logger.w(TAG, "Validation: NeedsReview — ${validationResult.issues}")
                    ProcessingStatus.NEEDS_REVIEW
                }
                is ExtractionValidator.ValidationResult.Failed -> {
                    logger.w(TAG, "Validation: Failed — ${validationResult.reason}")
                    ProcessingStatus.NEEDS_REVIEW  // Still save, but flag for review
                }
            }

            finishWithAiResult(docId, document, aiResult, bitmaps, status, bestBarcode)

        } catch (e: java.io.IOException) {
            logger.e(TAG, "Transient I/O error for document ID $docId", e)
            Result.retry()
        } catch (e: OutOfMemoryError) {
            logger.e(TAG, "OOM error for document ID $docId, retrying...", e)
            Result.retry()
        } catch (e: com.google.ai.client.generativeai.type.ServerException) {
            logger.e(TAG, "Gemini API Server Error (Rate Limit / Quota), retrying...", e)
            Result.retry()
        } catch (e: Exception) {
            // Also check if it's a 429 inside a generic exception message
            if (e.message?.contains("429") == true || e.message?.contains("quota") == true || e.message?.contains("Too Many Requests") == true) {
                logger.w(TAG, "Caught rate limit inside generic exception, retrying... Exception: ${e.message}")
                Result.retry()
            } else {
                logger.e(TAG, "Permanent error processing document ID $docId", e)
                documentDao.updateProcessingStatus(docId, ProcessingStatus.FAILED)
                showNotification(docId, "Processing Failed", "Could not analyse this document")
                Result.failure()
            }
        } finally {
            bitmapSet?.displayBitmap?.recycle()
            bitmapSet?.barcodeBitmap?.recycle()
        }
    }

    private suspend fun finishWithAiResult(
        docId: Long,
        document: com.pdfwallet.data.db.Document,
        aiResult: com.pdfwallet.service.ai.AiDocumentResult,
        bitmaps: List<Bitmap>,
        status: ProcessingStatus,
        bestBarcode: com.pdfwallet.service.pdf.RankedBarcode? = null
    ): Result {
        val mapped = AiResultMapper.map(aiResult)

        // Thumbnail
        val thumbnailPath = thumbnailGenerator.generateThumbnail(document.filePath)

        // Barcode values
        var mergedDocumentId = mapped.documentId
        if (mergedDocumentId.isNullOrEmpty() && bestBarcode != null) {
            mergedDocumentId = bestBarcode.rawValue
        }
        val finalBarcodes = bestBarcode?.rawValue

        // Save best barcode image
        bestBarcode?.bitmap?.let { barcodeExtractor.saveBarcodeImage(it, document.contentHash) }

        // Persist to Room
        val updatedDoc = document.copy(
            processingStatus    = status,
            thumbnailPath       = thumbnailPath,
            rawOcrText          = finalBarcodes,
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
            metadata            = mapped.ticketMetadata
        )
        documentDao.insert(updatedDoc)

        val notifTitle = if (status == ProcessingStatus.COMPLETE) "Added to Wallet ✓" else "Needs Review"
        showNotification(docId, notifTitle, mapped.title)
        logger.i(TAG, "Finished processing document ID $docId with status $status")
        return Result.success()
    }

    private fun buildFieldMap(mapped: AiResultMapper.MappedResult): Map<String, String?> {
        return mapOf(
            "passenger_name" to mapped.holderName,
            "full_name" to mapped.holderName,
            "primary_name" to mapped.holderName,
            "guest_name" to mapped.holderName,
            "holder_name" to mapped.holderName,
            "member_name" to mapped.holderName,
            "recipient_name" to mapped.holderName,
            "document_title" to mapped.title,
            "issue_date" to mapped.issueDate,
            "expiry_date" to mapped.expiryDate,
            "document_id" to mapped.documentId
        )
    }

    private fun showNotification(docId: Long, title: String, content: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return
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
