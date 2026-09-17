package com.pdfwallet.ui.pass

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

object MovieTemplate : TemplateContract {

    override val documentType = DocumentType.MOVIE

    override fun accentColors(isDark: Boolean) = DocAccent(
        primary      = Color(0xFF1F1F1F),  // dark grey almost black
        onPrimary    = Color(0xFFE5B800),  // Cinema Gold
        container    = Color(0xFF333333),
        onContainer  = Color(0xFFF0E68C),
        surface      = Color(0xFF121212),
        onSurface    = Color(0xFFFFFFFF),
    )

    @Composable
    override fun Content(document: Document, metadata: DocumentMetadata?, modifier: Modifier, barcodeContent: @Composable () -> Unit) {
        val meta = metadata as? DocumentMetadata.Movie
        val baseAccent = accentColors(isSystemInDarkTheme())
        val dynamicColor = rememberDominantColor(document.thumbnailPath, baseAccent.primary)
        val accent = baseAccent.copy(primary = dynamicColor.value)
        val flaggedFields = document.validationFlags

        Column(modifier = modifier
            .fillMaxWidth()
            .background(accent.primary)
            .padding(top = 16.dp, bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = accent.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = meta?.movieName ?: "CINEMA TICKET",
                    color = accent.onPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = accent.surface,
                shadowElevation = 4.dp
            ) {
                Column {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                        FieldRow2Col("Cinema", meta?.cinemaName, "Date", meta?.showDate, accent, flaggedFields)
                        Spacer(modifier = Modifier.height(16.dp))
                        FieldRow2Col("Time", meta?.showTime, "Seat", meta?.seatNumbers?.joinToString() ?: meta?.seats, accent, flaggedFields)
                    }

                    // Barcode Section
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        barcodeContent()
                    }
                }
            }
        }
    }

    @Composable
    override fun CompactCard(document: Document, metadata: DocumentMetadata?, modifier: Modifier) {
        val meta = metadata as? DocumentMetadata.Movie
        val baseAccent = accentColors(isSystemInDarkTheme())
        val dynamicColor = rememberDominantColor(document.thumbnailPath, baseAccent.primary)
        val accent = baseAccent.copy(primary = dynamicColor.value)
        Row(
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(accent.primary)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Movie, null, tint = accent.onPrimary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(meta?.movieName ?: "Movie Ticket", color = accent.onPrimary,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(meta?.showDate ?: "", color = accent.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
