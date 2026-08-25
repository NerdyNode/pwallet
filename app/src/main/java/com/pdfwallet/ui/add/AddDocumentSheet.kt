package com.pdfwallet.ui.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pdfwallet.ui.theme.Dimens

val GoogleBlue = Color(0xFF4285F4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onPdfClick: () -> Unit,
    onDriveClick: () -> Unit
) {
    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = Dimens.RadiusExtraLarge, topEnd = Dimens.RadiusExtraLarge),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(horizontal = Dimens.ScreenPaddingLarge)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Add Document",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                Text(
                    text = "Choose how you want to import",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))

                ImportOptionRow(
                    icon = Icons.Default.CameraAlt,
                    title = "Scan with Camera",
                    subtitle = "Take a photo of your document",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = {
                        onCameraClick()
                        onDismiss()
                    }
                )
                
                ImportOptionRow(
                    icon = Icons.Default.Image,
                    title = "Choose from Gallery",
                    subtitle = "Select image or PDF",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = {
                        onGalleryClick()
                        onDismiss()
                    }
                )
                
                ImportOptionRow(
                    icon = Icons.Default.Description,
                    title = "Choose PDF File",
                    subtitle = "Pick a PDF from storage",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = {
                        onPdfClick()
                        onDismiss()
                    }
                )
                
                ImportOptionRow(
                    icon = Icons.Default.Cloud,
                    title = "Google Drive",
                    subtitle = "Import from your Drive",
                    iconTint = GoogleBlue,
                    onClick = {
                        onDriveClick()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(Dimens.RadiusMedium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.SpacingNormal),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(Dimens.SpacingMedium))
                        Text(
                            text = "Supported Documents\nPDF, JPG, PNG up to 25MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun ImportOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimens.SpacingNormal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconTint.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(Dimens.SpacingNormal))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
