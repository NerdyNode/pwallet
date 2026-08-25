package com.pdfwallet.data.db

enum class DocumentCategory(val label: String, val types: List<DocumentType>) {
    TRAVEL("Travel", listOf(DocumentType.AIRLINE, DocumentType.TRAIN, DocumentType.BUS)),
    HOTEL("Hotel", listOf(DocumentType.HOTEL)),
    ID_PROOF("ID Proof", listOf(DocumentType.GOVERNMENT_ID, DocumentType.CERTIFICATE)),
    MEMBERSHIPS("Memberships", listOf(DocumentType.MEMBERSHIP)),
    TRANSIT("Transit", listOf(DocumentType.TRANSIT)),
    OTHER("Other", listOf(DocumentType.OTHER))
}
