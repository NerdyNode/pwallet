package com.pdfwallet.data.db

import androidx.room.TypeConverter
import com.pdfwallet.domain.model.ProcessingStatus
import com.pdfwallet.domain.model.DocumentType
import com.pdfwallet.domain.model.CaptureSource
import com.pdfwallet.domain.model.BookingStatus

class EnumConverters {

    // ProcessingStatus
    @TypeConverter
    fun fromProcessingStatus(status: ProcessingStatus): String = status.name

    @TypeConverter
    fun toProcessingStatus(value: String): ProcessingStatus =
        ProcessingStatus.entries.find { it.name == value } ?: ProcessingStatus.FAILED

    // DocumentType
    @TypeConverter
    fun fromDocumentType(type: DocumentType): String = type.name

    @TypeConverter
    fun toDocumentType(value: String): DocumentType =
        DocumentType.entries.find { it.name == value } ?: DocumentType.OTHER

    // CaptureSource
    @TypeConverter
    fun fromCaptureSource(source: CaptureSource): String = source.name

    @TypeConverter
    fun toCaptureSource(value: String): CaptureSource =
        CaptureSource.entries.find { it.name == value } ?: CaptureSource.MANUAL_IMPORT

    // BookingStatus
    @TypeConverter
    fun fromBookingStatus(status: BookingStatus?): String? = status?.name

    @TypeConverter
    fun toBookingStatus(value: String?): BookingStatus? =
        value?.let { BookingStatus.entries.find { e -> e.name == it } ?: BookingStatus.UNKNOWN }
}
