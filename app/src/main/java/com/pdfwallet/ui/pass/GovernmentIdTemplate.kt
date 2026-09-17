package com.pdfwallet.ui.pass

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
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

object GovernmentIdTemplate : TemplateContract {

    override val documentType = DocumentType.AADHAAR // Serves as base for all IDs

    override fun accentColors(isDark: Boolean) = if (isDark) DocAccent(
        primary      = Color(0xFF2E7D32),
        onPrimary    = Color(0xFFFFFFFF),
        container    = Color(0xFF1B5E20),
        onContainer  = Color(0xFFC8E6C9),
        surface      = Color(0xFF1A1A1A),
        onSurface    = Color(0xFFF5F5F5),
    ) else DocAccent(
        primary      = Color(0xFF4CAF50),
        onPrimary    = Color(0xFFFFFFFF),
        container    = Color(0xFFC8E6C9),
        onContainer  = Color(0xFF1B5E20),
        surface      = Color(0xFFFFFFFF),
        onSurface    = Color(0xFF212121),
    )

    @Composable
    override fun Content(document: Document, metadata: DocumentMetadata?, modifier: Modifier, barcodeContent: @Composable () -> Unit) {
        val meta = metadata as? DocumentMetadata.GovernmentId
        val baseAccent = accentColors(isSystemInDarkTheme())
        val dynamicColor = rememberDominantColor(document.thumbnailPath, baseAccent.primary)
        val accent = baseAccent.copy(primary = dynamicColor.value)
        val flaggedFields = document.validationFlags

        val title = when (document.documentType) {
            DocumentType.AADHAAR -> "AADHAAR CARD"
            DocumentType.PAN_CARD -> "PAN CARD"
            DocumentType.DRIVING_LICENSE -> "DRIVING LICENSE"
            DocumentType.PASSPORT -> "PASSPORT"
            else -> "IDENTITY CARD"
        }

        Column(modifier = modifier
            .fillMaxWidth()
            .background(accent.surface)
            .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.586f) // Standard ID card ratio (e.g. CR80)
                    .clip(RoundedCornerShape(12.dp)),
                color = accent.primary,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = accent.onPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.Badge, contentDescription = null, tint = accent.onPrimary)
                    }
                    
                    Column {
                        Text(
                            text = meta?.fullName ?: document.holderName ?: "—",
                            color = accent.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = meta?.idNumber ?: document.documentId ?: "—",
                            color = accent.onPrimary.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "DOB: " + (meta?.dateOfBirth ?: "—"),
                                color = accent.onPrimary.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelMedium
                            )
                            meta?.gender?.let { g ->
                                Text(
                                    text = g,
                                    color = accent.onPrimary.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            // Additional info below the card
            Spacer(modifier = Modifier.height(16.dp))

            meta?.fatherName?.let { fname ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text("Father's Name", style = MaterialTheme.typography.labelSmall,
                        color = accent.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f))
                    Text(fname, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent.onSurface,
                        modifier = Modifier.weight(2f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Barcode Section below the card visualization
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = accent.surface,
                shadowElevation = 4.dp
            ) {
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

    @Composable
    override fun CompactCard(document: Document, metadata: DocumentMetadata?, modifier: Modifier) {
        val meta = metadata as? DocumentMetadata.GovernmentId
        val baseAccent = accentColors(isSystemInDarkTheme())
        val dynamicColor = rememberDominantColor(document.thumbnailPath, baseAccent.primary)
        val accent = baseAccent.copy(primary = dynamicColor.value)
        val title = when (document.documentType) {
            DocumentType.AADHAAR -> "Aadhaar"
            DocumentType.PAN_CARD -> "PAN Card"
            DocumentType.DRIVING_LICENSE -> "License"
            DocumentType.PASSPORT -> "Passport"
            else -> "ID Card"
        }
        Row(
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(accent.primary)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Badge, null, tint = accent.onPrimary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = accent.onPrimary,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(meta?.fullName ?: "", color = accent.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
