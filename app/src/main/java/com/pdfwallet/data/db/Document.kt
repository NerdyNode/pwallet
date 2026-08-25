package com.pdfwallet.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProcessingStatus {
    PENDING,
    PROCESSING,
    COMPLETE,
    FAILED
}

enum class CaptureSource {
    MANUAL_IMPORT,
    SHARE_INTENT
}

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentType: DocumentType = DocumentType.OTHER,
    val importDate: Long,
    val filePath: String,
    val thumbnailPath: String?,
    val rawOcrText: String?,
    val title: String,
    val documentId: String?,
    val holderName: String?,
    val issueDate: String?,
    val expiryDate: String?,
    val sourceLocation: String?,
    val destinationLocation: String?,
    val additionalMeta: TicketMetadata?,
    val bookingStatus: BookingStatus? = null,
    val journeyDate: Long? = null,
    val processingStatus: ProcessingStatus = ProcessingStatus.PENDING,
    val contentHash: String,
    val captureSource: CaptureSource,
    @androidx.room.ColumnInfo(index = true) val expiryDateEpoch: Long? = null,
    
    // Phase 2 Production Fields
    val isSensitive: Boolean = false,
    val maskedIdentifier: String? = null,
    val collectionId: Long? = null,
    val localPath: String? = null
)
