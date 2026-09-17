package com.pdfwallet.data.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import com.pdfwallet.util.Logger
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger
) {
    private val pdfsDir = File(context.filesDir, "pdfs").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    /**
     * Copies the PDF from the given URI to app-private storage.
     * Returns a pair of (absolute file path, SHA-256 content hash).
     */
    suspend fun copyToPrivateStorage(uri: Uri): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            logger.i("PdfFileManager", "Starting copy from URI: $uri")
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val extension = when {
                mimeType.contains("image/png") -> ".png"
                mimeType.contains("image/jpeg") || mimeType.contains("image/jpg") -> ".jpg"
                mimeType.contains("image/webp") -> ".webp"
                else -> ".pdf"
            }
            val uniqueFilename = "${UUID.randomUUID()}$extension"
            val destFile = File(pdfsDir, uniqueFilename)
            
            val digest = MessageDigest.getInstance("SHA-256")
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        digest.update(buffer, 0, bytesRead)
                    }
                }
            } ?: throw IllegalArgumentException("Could not open input stream for URI: $uri")
            
            val hashBytes = digest.digest()
            val contentHash = hashBytes.joinToString("") { "%02x".format(it) }
            
            logger.i("PdfFileManager", "Successfully copied to ${destFile.absolutePath} (Hash: $contentHash)")
            Pair(destFile.absolutePath, contentHash)
        } catch (e: Exception) {
            logger.e("PdfFileManager", "Failed to copy file from URI: $uri", e)
            throw e
        }
    }
    fun isInPrivateStorage(uri: Uri): Boolean {
        if (uri.scheme != "file") return false
        val path = uri.path ?: return false
        val privatePath = pdfsDir.canonicalPath
        return File(path).canonicalPath.startsWith(privatePath)
    }

    fun hashFile(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
