package com.pdfwallet.data.local

import android.content.Context
import com.pdfwallet.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger
) {

    suspend fun backup(outputStream: OutputStream): Boolean = withContext(Dispatchers.IO) {
        try {
            ZipOutputStream(outputStream).use { zos ->
                // Backup Database
                val dbFile = context.getDatabasePath("pdf_wallet.db")
                val dbDir = dbFile.parentFile
                if (dbDir != null && dbDir.exists()) {
                    dbDir.listFiles()?.filter { it.name.startsWith("pdf_wallet.db") }?.forEach { file ->
                        addFileToZip(zos, file, "db/${file.name}")
                    }
                }

                // Backup PDFs
                val pdfsDir = File(context.filesDir, "pdfs")
                if (pdfsDir.exists()) {
                    pdfsDir.listFiles()?.forEach { file ->
                        if (file.isFile) {
                            addFileToZip(zos, file, "pdfs/${file.name}")
                        }
                    }
                }
            }
            logger.i("BackupRestore", "Backup completed successfully")
            true
        } catch (e: Exception) {
            logger.e("BackupRestore", "Backup failed", e)
            false
        }
    }

    data class RestoreResult(val isSuccess: Boolean, val pdfsRestored: Int = 0, val dbRestored: Boolean = false)

    suspend fun restore(inputStream: InputStream): RestoreResult = withContext(Dispatchers.IO) {
        try {
            var pdfCount = 0
            var dbRestored = false
            ZipInputStream(inputStream).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val path = entry.name
                        val targetFile = when {
                            path.startsWith("db/") -> {
                                dbRestored = true
                                val dbName = path.substringAfter("db/")
                                File(context.getDatabasePath("pdf_wallet.db").parentFile, dbName)
                            }
                            path.startsWith("pdfs/") -> {
                                pdfCount++
                                val pdfName = path.substringAfter("pdfs/")
                                val pdfsDir = File(context.filesDir, "pdfs")
                                if (!pdfsDir.exists()) pdfsDir.mkdirs()
                                File(pdfsDir, pdfName)
                            }
                            else -> null
                        }

                        targetFile?.let { file ->
                            val targetDir = if (path.startsWith("db/")) context.getDatabasePath("pdf_wallet.db").parentFile else File(context.filesDir, "pdfs")
                            val canonicalTarget = targetDir.canonicalPath
                            if (!file.canonicalPath.startsWith(canonicalTarget + File.separator) &&
                                file.canonicalPath != canonicalTarget) {
                                logger.w("BackupRestore", "Blocked Zip Slip attempt: ${entry.name}")
                                return@let
                            }
                            FileOutputStream(file).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            logger.i("BackupRestore", "Restore completed successfully: $pdfCount PDFs, DB=$dbRestored")
            RestoreResult(true, pdfCount, dbRestored)
        } catch (e: Exception) {
            logger.e("BackupRestore", "Restore failed", e)
            RestoreResult(false)
        }
    }

    private fun addFileToZip(zos: ZipOutputStream, file: File, zipPath: String) {
        FileInputStream(file).use { fis ->
            val zipEntry = ZipEntry(zipPath)
            zos.putNextEntry(zipEntry)
            fis.copyTo(zos)
            zos.closeEntry()
        }
    }
}
