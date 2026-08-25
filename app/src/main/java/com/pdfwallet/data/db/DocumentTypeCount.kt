package com.pdfwallet.data.db

import androidx.room.ColumnInfo

data class DocumentTypeCount(
    @ColumnInfo(name = "documentType") val type: DocumentType,
    @ColumnInfo(name = "count") val count: Int
)
