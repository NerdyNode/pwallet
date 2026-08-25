package com.pdfwallet.ui.pass

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdfwallet.ui.theme.DocAccent
import com.pdfwallet.ui.detail.TicketField
import com.pdfwallet.ui.theme.Dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PassCard(
    template: PassTemplate,
    accent: DocAccent,
    modifier: Modifier = Modifier,
    onCopy: (String, String) -> Unit = { _, _ -> }
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        PassHeader(template, accent)

        // Body content
        Column(modifier = Modifier.padding(Dimens.SpacingLarge)) {
            // Header fields (PNR, STATUS, etc.)
            if (template.headerFields.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    template.headerFields.forEachIndexed { index, field ->
                        val alignment = if (index == 0) Alignment.Start else Alignment.End
                        TicketField(
                            label = field.label,
                            value = field.value,
                            accent = accent,
                            alignment = alignment,
                            titleSize = if (field.emphasis == Emphasis.LARGE) 20.sp else 14.sp,
                            onCopy = onCopy
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
            }

            // Route section (FROM -> TO)
            template.routeSection?.let { route ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TicketField(
                            label = route.origin.label,
                            value = route.origin.value,
                            accent = accent,
                            titleSize = if (route.origin.emphasis == Emphasis.HERO) 20.sp else 14.sp,
                            onCopy = onCopy
                        )
                    }
                    Icon(
                        route.icon,
                        contentDescription = "to",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = Dimens.SpacingSmall)
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TicketField(
                            label = route.destination.label,
                            value = route.destination.value,
                            accent = accent,
                            alignment = Alignment.End,
                            titleSize = if (route.destination.emphasis == Emphasis.HERO) 20.sp else 14.sp,
                            onCopy = onCopy
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
            }

            // Body rows (generic field grid)
            template.bodyRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.fields.forEachIndexed { index, field ->
                        val alignment = when {
                            row.fields.size == 1 -> Alignment.Start
                            index == row.fields.lastIndex -> Alignment.End
                            else -> Alignment.Start
                        }
                        TicketField(
                            label = field.label,
                            value = field.value,
                            accent = accent,
                            alignment = alignment,
                            onCopy = onCopy
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
            }

            // Passenger list
            if (template.passengerList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                Text(
                    text = "PASSENGERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                template.passengerList.forEach { passenger ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = passenger.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = passenger.value,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Detail fields
            if (template.detailFields.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                template.detailFields.forEach { field ->
                    TicketField(
                        label = field.label,
                        value = field.value,
                        accent = accent,
                        onCopy = onCopy
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                }
            }
        }
    }
}

@Composable
private fun PassHeader(template: PassTemplate, accent: DocAccent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.primary)
            .padding(Dimens.SpacingLarge),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = template.typeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = accent.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            Text(
                text = template.title,
                style = MaterialTheme.typography.headlineMedium,
                color = accent.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
