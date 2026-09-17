package com.pdfwallet.ui.pass

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.DocumentMetadata
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.ui.theme.DocAccent

object TrainTemplate : TemplateContract {

    override val documentType = DocumentType.TRAIN

    override fun accentColors(isDark: Boolean) = if (isDark) DocAccent(
        primary      = Color(0xFFFF6D00),
        onPrimary    = Color(0xFFFFFFFF),
        container    = Color(0xFF5D2C00),
        onContainer  = Color(0xFFFFD180),
        surface      = Color(0xFF1C1C1C),
        onSurface    = Color(0xFFF5F5F5),
    ) else DocAccent(
        primary      = Color(0xFFFF6D00),
        onPrimary    = Color(0xFFFFFFFF),
        container    = Color(0xFFFFF3E0),
        onContainer  = Color(0xFFE65100),
        surface      = Color(0xFFFAFAFA),
        onSurface    = Color(0xFF212121),
    )

    @Composable
    override fun Content(document: Document, metadata: DocumentMetadata?, modifier: Modifier, barcodeContent: @Composable () -> Unit) {
        val meta = metadata as? DocumentMetadata.Train
        val accent = accentColors(isSystemInDarkTheme())

        Column(
            modifier
                .clip(RoundedCornerShape(16.dp))
                .background(accent.surface)
        ) {
            // ── Route Header ───────────────────────────────
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                RouteHeaderTrain(
                    originCode   = meta?.origin?.stationCode ?: meta?.boardingStation ?: "---",
                    originName   = meta?.origin?.stationName ?: "",
                    destCode     = meta?.destination?.stationCode ?: meta?.destinationStation ?: "---",
                    destName     = meta?.destination?.stationName ?: "",
                    departTime   = meta?.departureTime ?: "--:--",
                    departDate   = meta?.journeyDate ?: "",
                    arriveTime   = meta?.arrivalTime ?: "--:--",
                    arriveDate   = "",
                    duration     = meta?.duration ?: "",
                    accent       = accent
                )
            }

            HorizontalDivider(color = accent.onSurface.copy(alpha = 0.08f))

            // ── Field Grid ─────────────────────────────────
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                TrainFieldRow(meta?.passengerName ?: document.holderName, "Passenger", meta?.pnr ?: document.documentId, "PNR", accent)
                Spacer(Modifier.height(4.dp))
                TrainFieldRow(meta?.travelClass, "Class", meta?.coachNumber?.let {
                    listOfNotNull(it, meta.seatNumber, meta.berthType).joinToString(" · ")
                } ?: meta?.coach?.let { listOfNotNull(it, meta.berth).joinToString(" · ") }, "Coach / Seat", accent)
                Spacer(Modifier.height(4.dp))
                TrainFieldRow(meta?.quota, "Quota", meta?.chartStatus ?: meta?.bookingStatus?.name, "Status", accent)
                meta?.fare?.let { fare ->
                    Spacer(Modifier.height(4.dp))
                    TrainFieldRow(fare, "Fare", meta.distance?.let { "$it km" }, "Distance", accent)
                }
            }

            HorizontalDivider(color = accent.onSurface.copy(alpha = 0.08f))

            // ── Barcode ────────────────────────────────────
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                barcodeContent()
            }
        }
    }

    @Composable
    override fun CompactCard(document: Document, metadata: DocumentMetadata?, modifier: Modifier) {
        val meta = metadata as? DocumentMetadata.Train
        val accent = accentColors(isSystemInDarkTheme())
        Row(
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(accent.container)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${meta?.origin?.stationCode ?: meta?.boardingStation ?: "---"} → ${meta?.destination?.stationCode ?: meta?.destinationStation ?: "---"}",
                    color = accent.onContainer, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    listOfNotNull(meta?.trainName, meta?.travelClass).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = accent.onContainer.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(meta?.departureTime ?: "", color = accent.primary,
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(meta?.journeyDate ?: "", style = MaterialTheme.typography.labelSmall,
                    color = accent.onContainer.copy(alpha = 0.6f))
            }
        }
    }
}
