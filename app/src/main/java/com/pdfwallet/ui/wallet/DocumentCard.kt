package com.pdfwallet.ui.wallet

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.ProcessingStatus
import com.pdfwallet.data.db.TicketMetadata
import com.pdfwallet.ui.theme.Dimens
import com.pdfwallet.ui.theme.DocAccent
import com.pdfwallet.ui.theme.getDocAccent

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun SharedTransitionScope.DocumentCard(
    doc: Document,
    animatedVisibilityScope: AnimatedVisibilityScope,
    privacyMode: Boolean = false,
    onClick: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val accent = getDocAccent(doc.documentType, isDark)
    val hapticFeedback = LocalHapticFeedback.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { 
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (doc.processingStatus == ProcessingStatus.FAILED) {
                        onRetry()
                    }
                }
            )
            .sharedElement(
                rememberSharedContentState(key = "card-${doc.id}"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
        shape = RoundedCornerShape(Dimens.RadiusExtraLarge),
        colors = CardDefaults.elevatedCardColors(
            containerColor = accent.container.copy(alpha = 0.9f),
            contentColor = accent.onContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accent.container,
                            accent.container.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(Dimens.SpacingLarge)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Label
                Surface(
                    color = if (doc.processingStatus == ProcessingStatus.FAILED) MaterialTheme.colorScheme.error else accent.primary,
                    contentColor = if (doc.processingStatus == ProcessingStatus.FAILED) MaterialTheme.colorScheme.onError else accent.onPrimary,
                    shape = RoundedCornerShape(Dimens.RadiusFull),
                    modifier = Modifier.padding(end = Dimens.SpacingMedium)
                ) {
                    Text(
                        text = if (doc.processingStatus == ProcessingStatus.FAILED) "RETRY" else doc.documentType.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier
                            .padding(horizontal = Dimens.SpacingMedium, vertical = Dimens.SpacingSmall)
                            .clickable(enabled = doc.processingStatus == ProcessingStatus.FAILED) { onRetry() }
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (doc.processingStatus == ProcessingStatus.PENDING || doc.processingStatus == ProcessingStatus.PROCESSING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = accent.primary,
                        strokeWidth = 2.dp
                    )
                }

                if (doc.processingStatus == ProcessingStatus.FAILED) {
                    Text(
                        text = "⚠ Processing Failed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingNormal))

            Text(
                text = doc.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                color = accent.onContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!doc.holderName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                Text(
                    text = doc.holderName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = accent.onContainer.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))

            // Specific metadata layout
            val meta = doc.additionalMeta
            val formattedJourneyDate = androidx.compose.runtime.remember(doc.journeyDate) {
                doc.journeyDate?.let { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it)) } ?: "--"
            }
            
            val maskFn = { id: String? ->
                if (id == null) "--"
                else if (privacyMode || doc.isSensitive) {
                    doc.maskedIdentifier ?: if (id.length > 4) "**** **** ${id.takeLast(4)}" else "****"
                } else id
            }

            when (meta) {
                is TicketMetadata.Train -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailColumn("PNR", maskFn(doc.documentId), accent.onContainer)
                        DetailColumn("TRAIN", meta.trainNumber, accent.onContainer)
                        DetailColumn("DATE", meta.journeyDate, accent.onContainer)
                        DetailColumn("STATUS", meta.bookingStatus.name, accent.onContainer)
                    }
                }
                is TicketMetadata.Airline -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailColumn("FLIGHT", meta.flightNumber ?: "--", accent.onContainer)
                        DetailColumn("DATE", formattedJourneyDate, accent.onContainer)
                        DetailColumn("TIME", meta.departureTime ?: "--", accent.onContainer)
                        DetailColumn("PNR", maskFn(doc.documentId), accent.onContainer)
                    }
                }
                is TicketMetadata.Bus -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailColumn("OPERATOR", meta.operator ?: "--", accent.onContainer)
                        DetailColumn("DATE", formattedJourneyDate, accent.onContainer)
                        DetailColumn("TIME", meta.departureTime ?: "--", accent.onContainer)
                    }
                }
                is TicketMetadata.Hotel -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailColumn("CHECK-IN", meta.checkIn ?: "--", accent.onContainer)
                        DetailColumn("CHECK-OUT", meta.checkOut ?: "--", accent.onContainer)
                        DetailColumn("ROOMS", meta.roomDetails ?: "--", accent.onContainer)
                    }
                }
                is TicketMetadata.Transit -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailColumn("OPERATOR", meta.operator ?: "--", accent.onContainer)
                        DetailColumn("ROUTE", meta.route ?: "--", accent.onContainer)
                        DetailColumn("VALIDITY", meta.validity ?: "--", accent.onContainer)
                    }
                }
                is TicketMetadata.Membership -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailColumn("PROVIDER", meta.provider ?: "--", accent.onContainer)
                        DetailColumn("MEMBER", meta.memberName ?: "--", accent.onContainer)
                        DetailColumn("VALIDITY", meta.validity ?: "--", accent.onContainer)
                    }
                }
                is TicketMetadata.GovernmentId -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailColumn("ID NUMBER", maskFn(doc.documentId), accent.onContainer)
                        meta.dateOfBirth?.let { DetailColumn("DOB", if (privacyMode || doc.isSensitive) "****" else it, accent.onContainer) }
                    }
                }
                else -> {
                    // Fallback generic info
                    doc.documentId?.let {
                        Text(
                            text = "ID: ${maskFn(it)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = accent.onContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailColumn(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}
