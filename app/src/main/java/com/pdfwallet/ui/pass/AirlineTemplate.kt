package com.pdfwallet.ui.pass

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
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

object AirlineTemplate : TemplateContract {

    override val documentType = DocumentType.AIRLINE

    override fun accentColors(isDark: Boolean) = if (isDark) DocAccent(
        primary      = Color(0xFFE53935),  // deep red
        onPrimary    = Color(0xFFFFFFFF),
        container    = Color(0xFF7F0000),
        onContainer  = Color(0xFFFFCDD2),
        surface      = Color(0xFF1A1A1A),
        onSurface    = Color(0xFFF5F5F5),
    ) else DocAccent(
        primary      = Color(0xFFD32F2F),
        onPrimary    = Color(0xFFFFFFFF),
        container    = Color(0xFFFFCDD2),
        onContainer  = Color(0xFF7F0000),
        surface      = Color(0xFFFFFFFF),
        onSurface    = Color(0xFF212121),
    )

    @Composable
    override fun Content(document: Document, metadata: DocumentMetadata?, modifier: Modifier, barcodeContent: @Composable () -> Unit) {
        val meta = metadata as? DocumentMetadata.Airline
        val baseAccent = accentColors(isSystemInDarkTheme())
        val dynamicColor = rememberDominantColor(document.thumbnailPath, baseAccent.primary)
        val accent = baseAccent.copy(primary = dynamicColor.value)
        val flaggedFields = document.validationFlags

        Column(modifier = modifier
            .fillMaxWidth()
            .background(accent.primary)
            .padding(top = 16.dp, bottom = 16.dp)
        ) {

            // Header: Route
            RouteHeaderAirline(
                origin      = meta?.origin?.iataCode ?: meta?.source ?: "---",
                originCity  = meta?.origin?.cityName ?: "",
                dest        = meta?.destination?.iataCode ?: "---",
                destCity    = meta?.destination?.cityName ?: "",
                departure   = meta?.departureTime ?: "--:--",
                arrival     = meta?.arrivalTime ?: "--:--",
                duration    = meta?.duration ?: "",
                accent      = accent
            )

            Spacer(modifier = Modifier.height(24.dp))

            // White Card overlapping the red background
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = accent.surface,
                shadowElevation = 4.dp
            ) {
                Column {
                    // Body: Field Grid
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                        FieldRow2Col("Passenger", meta?.passengerName, "Flight", meta?.flightNumber, accent, flaggedFields)
                        Spacer(modifier = Modifier.height(16.dp))
                        FieldRow2Col("Date", meta?.departureDate, "Terminal", meta?.terminal, accent, flaggedFields)
                        Spacer(modifier = Modifier.height(16.dp))
                        FieldRow2Col("Class", meta?.travelClass, "Seat", meta?.seat, accent, flaggedFields)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val bag = meta?.baggageAllowance ?: "15kg"
                        val pnr = meta?.pnr ?: ""
                        FieldRow2Col("Baggage Allowance", bag, "PNR", pnr, accent, flaggedFields)
                        
                        if (meta?.gate != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            FieldRow2Col("Gate", meta.gate, "Sequence", meta.sequenceNumber, accent, flaggedFields)
                        }
                    }

                    // Perforation
                    PerforationDivider(accent.onSurface.copy(alpha = 0.2f))

                    // Barcode Stub
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        barcodeContent()
                    }
                }
            }
        }
    }

    @Composable
    override fun CompactCard(document: Document, metadata: DocumentMetadata?, modifier: Modifier) {
        val meta = metadata as? DocumentMetadata.Airline
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
            Text(meta?.origin?.iataCode ?: meta?.source ?: "---", color = accent.onPrimary,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.FlightTakeoff, null, tint = accent.onPrimary.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp))
            Text(meta?.destination?.iataCode ?: "---", color = accent.onPrimary,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(meta?.flightNumber ?: "", color = accent.onPrimary,
                    style = MaterialTheme.typography.labelMedium)
                Text(meta?.departureTime ?: "", color = accent.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
