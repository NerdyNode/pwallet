package com.pdfwallet.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pdfwallet.data.db.DocumentDao
import com.pdfwallet.util.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
class StorageCleanupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val documentDao: DocumentDao,
    private val logger: Logger
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "StorageCleanupWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            logger.i(TAG, "Starting storage cleanup task...")
            
            val pdfsDir = File(context.filesDir, "pdfs")
            if (!pdfsDir.exists() || !pdfsDir.isDirectory) {
                return@withContext Result.success()
            }

            // Get all valid file paths from DB
            val allDocuments = documentDao.getAllDocuments().first()
            
            // Build a set of allowed paths
            val allowedPaths = mutableSetOf<String>()
            allDocuments.forEach { doc ->
                allowedPaths.add(doc.filePath)
                doc.thumbnailPath?.let { allowedPaths.add(it) }
            }

            var deletedCount = 0
            var reclaimedBytes = 0L

            // Iterate over all files in the directory
            pdfsDir.listFiles()?.forEach { file ->
                if (!allowedPaths.contains(file.absolutePath)) {
                    val size = file.length()
                    if (file.delete()) {
                        deletedCount++
                        reclaimedBytes += size
                        logger.d(TAG, "Deleted orphaned file: ")
                    } else {
                        logger.w(TAG, "Failed to delete orphaned file: ")
                    }
                }
            }

            logger.i(TAG, "Storage cleanup complete. Deleted ${deletedCount} orphaned files. Reclaimed ${reclaimedBytes / 1024} KB.")
            Result.success()
        } catch (e: Exception) {
            logger.e(TAG, "Error running storage cleanup", e)
            Result.failure()
        }
    }
}
