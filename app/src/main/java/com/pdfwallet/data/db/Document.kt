package com.pdfwallet.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

enum class ProcessingStatus {
    PENDING,
    PROCESSING,
    COMPLETE,
    FAILED,
    NEEDS_RESCAN,
    NEEDS_REVIEW
}

enum class CaptureSource {
    MANUAL_IMPORT,
    SHARE_INTENT
}

enum class TextQualityTier {
    RICH, DECENT, SPARSE, POOR, UNKNOWN
}

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentType: DocumentType = DocumentType.UNKNOWN,
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
    @ColumnInfo(name = "additionalMeta") val metadata: DocumentMetadata?,
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
    val localPath: String? = null,
    
    // Architecture V2 Upgrade Fields
    val aiConfidence: Float = 0f,
    val textQualityTier: TextQualityTier = TextQualityTier.UNKNOWN,
    val validationFlags: List<String> = emptyList()
)
