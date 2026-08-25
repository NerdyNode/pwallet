package com.pdfwallet.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_documents")
data class OfflineDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val addedAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)
