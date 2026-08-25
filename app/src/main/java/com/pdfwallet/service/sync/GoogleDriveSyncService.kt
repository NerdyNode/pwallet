package com.pdfwallet.service.sync

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import com.pdfwallet.data.db.AppDatabase

class GoogleDriveSyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase
) {
    private fun getDriveService(email: String): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccountName = email
        }
        
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("PdfWallet")
            .build()
    }

    suspend fun backupDatabase(email: String, dbFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(email)
            
            // Checkpoint WAL to flush all data into the main .db file
            try {
                appDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)", emptyArray())
            } catch (e: Exception) {
                // Non-fatal — backup will still work, just may miss very recent writes
            }

            // Create a temporary ZIP file containing the DB and PDFs
            val tempZip = File(context.cacheDir, "wallet_backup.zip")
            val backupKey = getOrCreateBackupKey()
            encryptStream(FileOutputStream(tempZip), backupKey).use { encOut ->
                ZipOutputStream(encOut).use { zos ->
                    // 1. Backup Database
                    val dbDir = dbFile.parentFile
                    if (dbDir != null && dbDir.exists()) {
                        dbDir.listFiles()?.filter { it.name.startsWith("pdf_wallet.db") }?.forEach { file ->
                            addFileToZip(zos, file, "db/${file.name}")
                        }
                    }

                    // 2. Backup PDFs
                    val pdfsDir = File(context.filesDir, "pdfs")
                    if (pdfsDir.exists()) {
                        pdfsDir.listFiles()?.forEach { file ->
                            if (file.isFile) {
                                addFileToZip(zos, file, "pdfs/${file.name}")
                            }
                        }
                    }
                }
            }

            // Check if backup already exists
            val fileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='pdf_wallet_backup.zip'")
                .execute()
                
            val fileContent = FileContent("application/zip", tempZip)
            
            if (fileList.files.isNotEmpty()) {
                val existingFileId = fileList.files[0].id
                driveService.files().update(existingFileId, null, fileContent).execute()
            } else {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = "pdf_wallet_backup.zip"
                    parents = listOf("appDataFolder")
                }
                driveService.files().create(fileMetadata, fileContent).execute()
            }
            
            tempZip.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreDatabase(email: String, destinationFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(email)
            
            val fileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='pdf_wallet_backup.zip'")
                .execute()
                
            if (fileList.files.isNotEmpty()) {
                val fileId = fileList.files[0].id
                val tempZip = File(context.cacheDir, "wallet_restore.zip")
                FileOutputStream(tempZip).use { outputStream ->
                    driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
                }

                // Extract ZIP
                val backupKey = getOrCreateBackupKey()
                decryptStream(FileInputStream(tempZip), backupKey).use { decIn ->
                    ZipInputStream(decIn).use { zis ->
                        var entry: ZipEntry? = zis.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory) {
                                val path = entry.name
                                val targetFile = when {
                                    path.startsWith("db/") -> {
                                        val dbName = path.substringAfter("db/")
                                        File(destinationFile.parentFile, dbName)
                                    }
                                    path.startsWith("pdfs/") -> {
                                        val pdfName = path.substringAfter("pdfs/")
                                        val pdfsDir = File(context.filesDir, "pdfs")
                                        if (!pdfsDir.exists()) pdfsDir.mkdirs()
                                        File(pdfsDir, pdfName)
                                    }
                                    else -> null
                                }

                                targetFile?.let { file ->
                                    FileOutputStream(file).use { fos ->
                                        zis.copyTo(fos)
                                    }
                                }
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                }
                tempZip.delete()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No backup found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

    private fun getOrCreateBackupKey(): javax.crypto.SecretKey {
        val masterKey = androidx.security.crypto.MasterKey.Builder(context)
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = androidx.security.crypto.EncryptedSharedPreferences.create(
            context, "pdf_wallet_backup_prefs", masterKey,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        var keyBase64 = prefs.getString("backup_encryption_key", null)
        if (keyBase64 == null) {
            val keyBytes = ByteArray(32)
            java.security.SecureRandom().nextBytes(keyBytes)
            keyBase64 = android.util.Base64.encodeToString(keyBytes, android.util.Base64.NO_WRAP)
            prefs.edit().putString("backup_encryption_key", keyBase64).apply()
        }
        val keyBytes = android.util.Base64.decode(keyBase64, android.util.Base64.NO_WRAP)
        return javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
    }

    private fun encryptStream(outputStream: java.io.OutputStream, key: javax.crypto.SecretKey): javax.crypto.CipherOutputStream {
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key)
        val dataOut = java.io.DataOutputStream(outputStream)
        dataOut.writeInt(cipher.iv.size)
        dataOut.write(cipher.iv)
        return javax.crypto.CipherOutputStream(outputStream, cipher)
    }

    private fun decryptStream(inputStream: java.io.InputStream, key: javax.crypto.SecretKey): javax.crypto.CipherInputStream {
        val dataIn = java.io.DataInputStream(inputStream)
        val ivSize = dataIn.readInt()
        val iv = ByteArray(ivSize)
        dataIn.readFully(iv)
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, javax.crypto.spec.GCMParameterSpec(128, iv))
        return javax.crypto.CipherInputStream(inputStream, cipher)
    }
}
