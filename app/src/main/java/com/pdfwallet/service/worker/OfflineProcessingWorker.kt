package com.pdfwallet.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pdfwallet.data.db.OfflineDocumentDao
import com.pdfwallet.data.repository.DocumentRepository
import com.pdfwallet.util.Logger
import com.pdfwallet.util.NetworkMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OfflineProcessingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val offlineDocumentDao: OfflineDocumentDao,
    private val documentRepository: DocumentRepository,
    private val networkMonitor: NetworkMonitor,
    private val logger: Logger
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!networkMonitor.isConnected()) {
            logger.i("OfflineProcessingWorker", "Still offline, postponing processing.")
            return Result.retry()
        }

        return try {
            val pendingDocs = offlineDocumentDao.getAllPending()
            if (pendingDocs.isEmpty()) {
                logger.i("OfflineProcessingWorker", "No pending offline documents to process.")
                return Result.success()
            }

            logger.i("OfflineProcessingWorker", "Found ${pendingDocs.size} pending documents. Resuming processing.")
            for (doc in pendingDocs) {
                try {
                    // Use file path directly to avoid URI parsing issues
                    val uri = android.net.Uri.fromFile(java.io.File(doc.filePath))
                    documentRepository.captureDocument(uri, com.pdfwallet.data.db.CaptureSource.MANUAL_IMPORT)
                    offlineDocumentDao.delete(doc.id)
                } catch (e: Exception) {
                    logger.e("OfflineProcessingWorker", "Failed to resume processing for doc ${doc.id}", e)
                    // Continue with next one, this one will be retried in next worker run
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            logger.e("OfflineProcessingWorker", "Error processing offline queue", e)
            Result.retry()
        }
    }
}
