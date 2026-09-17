package com.pdfwallet.ui.pass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.DocumentMetadata
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.ui.theme.DocAccent

interface TemplateContract {
    val documentType: DocumentType

    // The accent colors for this document type
    fun accentColors(isDark: Boolean): DocAccent

    // The layout composable — owns its own structure entirely
    @Composable
    fun Content(document: Document, metadata: DocumentMetadata?, modifier: Modifier, barcodeContent: @Composable () -> Unit = {})

    // The compact card variant (for wallet list view)
    @Composable
    fun CompactCard(document: Document, metadata: DocumentMetadata?, modifier: Modifier)
}
