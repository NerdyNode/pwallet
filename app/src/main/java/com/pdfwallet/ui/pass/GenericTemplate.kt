package com.pdfwallet.ui.pass

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.DocumentMetadata
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.ui.theme.DocAccent

object GenericTemplate : TemplateContract {

    override val documentType = DocumentType.GENERIC

    override fun accentColors(isDark: Boolean) = if (isDark) DocAccent(
        primary      = Color(0xFF1976D2),
        onPrimary    = Color(0xFFFFFFFF),
        container    = Color(0xFF004BA0),
        onContainer  = Color(0xFFBBDEFB),
        surface      = Color(0xFF1E1E1E),
        onSurface    = Color(0xFFF5F5F5),
    ) else DocAccent(
        primary      = Color(0xFF1976D2),
        onPrimary    = Color(0xFFFFFFFF),
        container    = Color(0xFFBBDEFB),
        onContainer  = Color(0xFF004BA0),
        surface      = Color(0xFFFFFFFF),
        onSurface    = Color(0xFF212121),
    )

    @Composable
    override fun Content(document: Document, metadata: DocumentMetadata?, modifier: Modifier, barcodeContent: @Composable () -> Unit) {
        val accent = accentColors(isSystemInDarkTheme())
        Column(modifier.clip(RoundedCornerShape(16.dp)).background(accent.surface).padding(16.dp)) {
            Text(document.title, style = MaterialTheme.typography.titleLarge, color = accent.onSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            document.holderName?.let {
                Text("Name", style = MaterialTheme.typography.labelSmall, color = accent.onSurface.copy(alpha = 0.5f))
                Text(it, style = MaterialTheme.typography.bodyLarge, color = accent.onSurface, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
            }
            document.documentId?.let {
                Text("ID / Reference", style = MaterialTheme.typography.labelSmall, color = accent.onSurface.copy(alpha = 0.5f))
                Text(it, style = MaterialTheme.typography.bodyLarge, color = accent.onSurface, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
            }
            val dates = listOfNotNull(
                document.issueDate?.let { "Issued: $it" },
                document.expiryDate?.let { "Expires: $it" }
            )
            if (dates.isNotEmpty()) {
                Text(dates.joinToString("  •  "), style = MaterialTheme.typography.bodyMedium, color = accent.onSurface.copy(alpha = 0.7f))
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))
            barcodeContent()
        }
    }

    @Composable
    override fun CompactCard(document: Document, metadata: DocumentMetadata?, modifier: Modifier) {
        val accent = accentColors(isSystemInDarkTheme())
        Row(
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(accent.container)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(document.title, color = accent.onContainer, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(document.documentType.name, style = MaterialTheme.typography.labelSmall, color = accent.onContainer.copy(alpha = 0.7f))
            }
        }
    }
}
