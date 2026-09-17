package com.pdfwallet.ui.pass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pdfwallet.data.db.Document
import com.pdfwallet.ui.theme.DocAccent

@Composable
fun PassCard(
    document: Document,
    modifier: Modifier = Modifier,
    onCopy: (String, String) -> Unit = { _, _ -> },
    barcodeContent: @Composable () -> Unit = {}
) {
    val template = TemplateRegistry.resolve(document.documentType)
    template.Content(document, document.metadata, modifier, barcodeContent)
}

@Composable
fun CompactPassCard(
    document: Document,
    modifier: Modifier = Modifier
) {
    val template = TemplateRegistry.resolve(document.documentType)
    template.CompactCard(document, document.metadata, modifier)
}
