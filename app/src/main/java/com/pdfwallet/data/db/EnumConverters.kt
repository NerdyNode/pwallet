package com.pdfwallet.data.db

import androidx.room.TypeConverter

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
        DocumentType.entries.find { it.name == value } ?: DocumentType.UNKNOWN

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

    // TextQualityTier
    @TypeConverter
    fun fromTextQualityTier(tier: TextQualityTier): String = tier.name

    @TypeConverter
    fun toTextQualityTier(value: String): TextQualityTier =
        TextQualityTier.entries.find { it.name == value } ?: TextQualityTier.UNKNOWN

    // List<String> for validationFlags
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString("||")

    @TypeConverter
    fun toStringList(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split("||")
}
