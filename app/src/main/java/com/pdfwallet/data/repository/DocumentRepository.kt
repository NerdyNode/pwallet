package com.pdfwallet.data.repository

import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pdfwallet.data.db.CaptureSource
import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.DocumentDao
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.data.db.ProcessingStatus
import com.pdfwallet.data.local.PdfFileManager
import com.pdfwallet.service.worker.DocumentProcessingWorker
import com.pdfwallet.util.Logger
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import com.pdfwallet.data.db.OfflineDocumentDao
import com.pdfwallet.data.db.OfflineDocumentEntity
import com.pdfwallet.util.NetworkMonitor
import androidx.work.Constraints
import androidx.work.NetworkType
import com.pdfwallet.service.worker.OfflineProcessingWorker

@Singleton
class DocumentRepository @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val documentDao: DocumentDao,
    private val offlineDocumentDao: OfflineDocumentDao,
    private val pdfFileManager: PdfFileManager,
    private val workManager: WorkManager,
    private val networkMonitor: NetworkMonitor,
    private val logger: Logger
) {
    val allDocuments: Flow<List<Document>> = documentDao.getAllDocuments()
    
    fun getPagedDocuments(): Flow<PagingData<Document>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { documentDao.getPagedDocuments() }
        ).flow
    }

    val documentTypeCounts = documentDao.getDocumentTypeCounts()
    val mostFrequentHolderName = documentDao.getMostFrequentHolderName()

    fun searchDocuments(query: String, types: List<DocumentType>, sort: String): Flow<List<Document>> {
        return documentDao.searchDocuments("%$query%", types, types.size, sort)
    }

    fun getDocumentsExpiringBefore(epochMillis: Long): Flow<List<Document>> {
        return documentDao.getDocumentsExpiringBefore(epochMillis)
    }

    suspend fun getDocumentById(id: Long): Document? {
        return documentDao.getById(id)
    }

    suspend fun captureDocument(uri: Uri, source: CaptureSource) {
        try {
            logger.i("DocumentRepository", "Starting captureDocument for URI: $uri (Source: $source)")
            
            // If the URI is already a file in our private storage, we don't need to copy it again
            // (e.g. from OfflineProcessingWorker). But since OfflineProcessingWorker calls this with `file://`, we just copy it over itself, which is fine, but it might change the hash. 
            // Better to check if it's already in private storage.
            val (filePath, contentHash) = if (pdfFileManager.isInPrivateStorage(uri)) {
                Pair(uri.path!!, pdfFileManager.hashFile(java.io.File(uri.path!!)))
            } else {
                pdfFileManager.copyToPrivateStorage(uri)
            }
            
            // Deduplication
            val existingDoc = documentDao.getByContentHash(contentHash)
            if (existingDoc != null) {
                logger.i("DocumentRepository", "Duplicate document detected (Hash: $contentHash), skipping.")
                return
            }

            if (!networkMonitor.isConnected()) {
                logger.i("DocumentRepository", "No internet connection. Adding to offline queue.")
                offlineDocumentDao.insert(OfflineDocumentEntity(filePath = filePath))
                
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                
                val workRequest = OneTimeWorkRequestBuilder<OfflineProcessingWorker>()
                    .setConstraints(constraints)
                    .build()
                    
                workManager.enqueueUniqueWork(
                    "OfflineProcessing",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    workRequest
                )
                return
            }

            val newDoc = Document(
                importDate = System.currentTimeMillis(),
                filePath = filePath,
                thumbnailPath = null,
                rawOcrText = null,
                title = "Processing...",
                documentId = null,
                holderName = null,
                issueDate = null,
                expiryDate = null,
                sourceLocation = null,
                destinationLocation = null,
                additionalMeta = null,
                processingStatus = ProcessingStatus.PENDING,
                contentHash = contentHash,
                captureSource = source
            )
            val docId = documentDao.insert(newDoc)
            logger.i("DocumentRepository", "Inserted new document with ID $docId")

            val workRequest = OneTimeWorkRequestBuilder<DocumentProcessingWorker>()
                .setInputData(Data.Builder().putLong(DocumentProcessingWorker.KEY_DOCUMENT_ID, docId).build())
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    10,
                    java.util.concurrent.TimeUnit.SECONDS
                )
                .build()
            
            workManager.enqueue(workRequest)
            logger.i("DocumentRepository", "Enqueued WorkManager task for document ID $docId")
        } catch (e: Exception) {
            logger.e("DocumentRepository", "Failed to capture document from URI: $uri", e)
            throw e
        }
    }
    suspend fun retryProcessing(id: Long) {
        val doc = documentDao.getById(id)
        if (doc != null) {
            documentDao.updateProcessingStatus(id, ProcessingStatus.PENDING)
            val workRequest = OneTimeWorkRequestBuilder<DocumentProcessingWorker>()
                .setInputData(Data.Builder().putLong(DocumentProcessingWorker.KEY_DOCUMENT_ID, id).build())
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    10,
                    java.util.concurrent.TimeUnit.SECONDS
                )
                .build()
            workManager.enqueue(workRequest)
        }
    }

    suspend fun deleteDocument(id: Long) {
        val doc = documentDao.getById(id)
        if (doc != null) {
            
            // Delete files from storage independently
            runCatching { java.io.File(doc.filePath).delete() }
                .onFailure { logger.e("DocumentRepository", "Failed to delete main file for document: ${doc.id}", it) }
                
            doc.thumbnailPath?.let { path ->
                runCatching { java.io.File(path).delete() }
                    .onFailure { logger.e("DocumentRepository", "Failed to delete thumbnail for document: ${doc.id}", it) }
            }
                
            val barcodeFile = java.io.File(context.filesDir, "barcodes/${doc.contentHash}.png")
            if (barcodeFile.exists()) {
                runCatching { barcodeFile.delete() }
                    .onFailure { logger.e("DocumentRepository", "Failed to delete barcode for document: ${doc.id}", it) }
            }
            
            // Always delete the DB record last
            documentDao.delete(doc)
        }
    }
}
