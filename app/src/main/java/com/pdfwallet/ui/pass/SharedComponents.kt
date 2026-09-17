package com.pdfwallet.ui.pass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pdfwallet.ui.theme.DocAccent

@Composable
fun PerforationDivider(
    color: Color,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left notch circle
        Box(Modifier.size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background))
        // Dashed line
        Canvas(Modifier.weight(1f).height(1.dp)) {
            drawLine(
                color = color,
                start = Offset.Zero,
                end = Offset(size.width, 0f),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
        label?.let {
            Text(it, style = MaterialTheme.typography.labelSmall,
                color = color, modifier = Modifier.padding(horizontal = 8.dp))
        }
        Canvas(Modifier.weight(1f).height(1.dp)) {
            drawLine(color, Offset.Zero, Offset(size.width, 0f),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                strokeWidth = 1.dp.toPx())
        }
        // Right notch circle
        Box(Modifier.size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background))
    }
}

@Composable
fun RouteLineDotted(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Canvas(Modifier.fillMaxWidth().height(2.dp).padding(vertical = 4.dp)) {
            drawLine(
                color = color,
                start = Offset.Zero,
                end = Offset(size.width, 0f),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                strokeWidth = 2.dp.toPx()
            )
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun RouteHeaderAirline(
    origin: String, originCity: String,
    dest: String, destCity: String,
    departure: String, arrival: String,
    duration: String, accent: DocAccent,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Origin
            Column {
                Text(originCity, style = MaterialTheme.typography.labelSmall,
                    color = accent.onPrimary.copy(alpha = 0.7f))
                Text(origin, style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black, color = accent.onPrimary)
            }
            // Route line
            RouteLineDotted(
                icon = Icons.Default.FlightTakeoff,
                label = duration,
                color = accent.onPrimary.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            // Destination
            Column(horizontalAlignment = Alignment.End) {
                Text(destCity, style = MaterialTheme.typography.labelSmall,
                    color = accent.onPrimary.copy(alpha = 0.7f))
                Text(dest, style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black, color = accent.onPrimary)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(departure, style = MaterialTheme.typography.titleMedium,
                color = accent.onPrimary)
            Spacer(Modifier.weight(1f))
            Text(arrival, style = MaterialTheme.typography.titleMedium,
                color = accent.onPrimary)
        }
    }
}

@Composable
fun RouteHeaderTrain(
    originCode: String, originName: String,
    destCode: String, destName: String,
    departTime: String, departDate: String,
    arriveTime: String, arriveDate: String,
    duration: String, accent: DocAccent,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                originCode,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = accent.primary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            // Dotted route line with duration pill in center
            Box(Modifier.weight(1f).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxWidth().height(2.dp)) {
                    drawLine(accent.primary.copy(alpha = 0.4f), Offset.Zero,
                        Offset(size.width, 0f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
                        strokeWidth = 2.dp.toPx())
                }
                if (duration.isNotBlank()) {
                    Surface(
                        color = accent.container,
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(duration, style = MaterialTheme.typography.labelSmall,
                            color = accent.onContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
            Text(
                destCode,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = accent.primary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(originName, style = MaterialTheme.typography.labelSmall,
                color = accent.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Text(destName, style = MaterialTheme.typography.labelSmall,
                color = accent.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            Column {
                Text(departTime, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = accent.onSurface)
                Text(departDate, style = MaterialTheme.typography.labelSmall,
                    color = accent.onSurface.copy(alpha = 0.5f))
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(arriveTime, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = accent.onSurface)
                Text(arriveDate, style = MaterialTheme.typography.labelSmall,
                    color = accent.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun FieldCell(
    label: String,
    value: String?,
    isFlagged: Boolean,
    accent: DocAccent,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
                color = if (isFlagged) Color(0xFFE65100) else accent.onSurface.copy(alpha = 0.6f))
            if (isFlagged) {
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Warning, null, tint = Color(0xFFE65100), modifier = Modifier.size(12.dp))
            }
        }
        Text(value ?: "--", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = accent.onSurface)
    }
}

@Composable
fun FieldRow2Col(
    label1: String, value1: String?,
    label2: String, value2: String?,
    accent: DocAccent,
    flaggedFields: List<String> = emptyList()
) {
    val isFlagged1 = label1.lowercase() in flaggedFields
    val isFlagged2 = label2.lowercase() in flaggedFields
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        FieldCell(label1, value1, isFlagged1, accent, Modifier.weight(1f))
        FieldCell(label2, value2, isFlagged2, accent, Modifier.weight(1f))
    }
}

@Composable
fun TrainFieldRow(
    value1: String?, label1: String,
    value2: String?, label2: String,
    accent: DocAccent
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        FieldCell(label1, value1, false, accent, Modifier.weight(1f))
        FieldCell(label2, value2, false, accent, Modifier.weight(1f))
    }
}

@Composable
fun ConfidenceBadge(confidence: Float, flags: List<String>) {
    when {
        confidence >= 0.85f -> Unit
        confidence >= 0.6f  -> {
            Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFC107)))
        }
        else -> {
            Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(4.dp)) {
                Row(
                    Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null,
                        tint = Color(0xFFE65100), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Review ${flags.size} field${if (flags.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE65100))
                }
            }
        }
    }
}
