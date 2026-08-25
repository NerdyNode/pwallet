package com.pdfwallet.ui.detail

import android.graphics.BitmapFactory

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.app.Activity
import android.view.WindowManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.data.db.TicketMetadata
import com.pdfwallet.ui.theme.Dimens
import com.pdfwallet.ui.theme.DocAccent
import com.pdfwallet.ui.theme.getDocAccent
import com.pdfwallet.util.BarcodeGenerator
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import android.content.Intent
import java.io.File

class TicketShape(private val cutoutRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val cornerRadius = 16.dp.value * density.density
            val r = cutoutRadius * density.density
            
            // Top Left
            moveTo(0f, cornerRadius)
            quadraticTo(0f, 0f, cornerRadius, 0f)
            
            // Top Right
            lineTo(size.width - cornerRadius, 0f)
            quadraticTo(size.width, 0f, size.width, cornerRadius)
            
            // Right Edge to Cutout
            val cutoutY = size.height * 0.35f
            lineTo(size.width, cutoutY - r)
            arcTo(
                rect = Rect(
                    left = size.width - r,
                    top = cutoutY - r,
                    right = size.width + r,
                    bottom = cutoutY + r
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            
            // Right Edge from Cutout to Bottom
            lineTo(size.width, size.height - cornerRadius)
            quadraticTo(size.width, size.height, size.width - cornerRadius, size.height)
            
            // Bottom Left
            lineTo(cornerRadius, size.height)
            quadraticTo(0f, size.height, 0f, size.height - cornerRadius)
            
            // Left Edge from Bottom to Cutout
            lineTo(0f, cutoutY + r)
            arcTo(
                rect = Rect(
                    left = -r,
                    top = cutoutY - r,
                    right = r,
                    bottom = cutoutY + r
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            
            // Left Edge to Top
            lineTo(0f, cornerRadius)
            close()
        }
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.TicketDetailScreen(
    docId: Long,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateBack: () -> Unit,
    viewModel: TicketDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(docId) {
        viewModel.loadDocument(docId)
    }

    val document by viewModel.document.collectAsStateWithLifecycle()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isExpanded = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Ticket") },
            text = { Text("This ticket will be permanently deleted. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteDocument {
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(document?.title ?: "Document Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        showDeleteConfirm = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Document", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            document?.let { doc ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.ScreenPadding)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingNormal)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val file = File(doc.filePath)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Ticket"))
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(Dimens.RadiusFull)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Share")
                        }
                        
                        Button(
                            onClick = {
                                val file = File(doc.filePath)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle no PDF viewer installed
                                    val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(fallbackIntent, "Save or View PDF"))
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(Dimens.RadiusFull)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Download PDF")
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (document != null) {
            val doc = document!!
            val isDark = isSystemInDarkTheme()
            val accent = getDocAccent(doc.documentType, isDark)

            if (isExpanded) {
                // Two-pane layout for tablets/desktop
                Row(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(Dimens.ScreenPaddingLarge),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingLarge)
                ) {
                    // Left Pane: Document Info
                    Box(modifier = Modifier.weight(1f)) {
                        TicketContent(doc, accent, animatedVisibilityScope) { label, value ->
                            scope.launch {
                                snackbarHostState.showSnackbar("$label copied")
                            }
                        }
                    }
                    
                    // Right Pane: Preview / Actions
                    Box(modifier = Modifier.weight(1f)) {
                        // PDF preview would go here in an expanded view
                    }
                }
            } else {
                // Single column layout for compact/medium
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpacingNormal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TicketContent(doc, accent, animatedVisibilityScope) { label, value ->
                        scope.launch {
                            snackbarHostState.showSnackbar("$label copied")
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.TicketContent(
    doc: Document,
    accent: DocAccent,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onCopy: (String, String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .sharedElement(
                rememberSharedContentState(key = "card-${doc.id}"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
        shape = TicketShape(16f), // custom boarding pass shape
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
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
                        text = doc.documentType.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent.onPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = accent.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Body content area before cutout
            Column(modifier = Modifier.padding(Dimens.SpacingLarge)) {
                val template = com.pdfwallet.ui.pass.PassTemplateResolver.resolve(doc)
                com.pdfwallet.ui.pass.PassCard(template = template, accent = accent, onCopy = onCopy)
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
            
            // Perforated Line across the cutout
            val dashedLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .drawBehind {
                        drawLine(
                            color = dashedLineColor,
                            start = Offset(16.dp.toPx(), 0f),
                            end = Offset(size.width - 16.dp.toPx(), 0f),
                            strokeWidth = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                        )
                    }
            )
            
            // Bottom part of the ticket (Barcode)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(Dimens.SpacingExtraLarge),
                contentAlignment = Alignment.Center
            ) {
                BarcodeSection(doc, accent)
            }
        }
    }
}

@Composable
fun BarcodeSection(doc: Document, accent: DocAccent) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val originalBrightness = window?.attributes?.screenBrightness ?: -1f
        window?.attributes = window?.attributes?.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        onDispose {
            window?.attributes = window?.attributes?.apply {
                screenBrightness = originalBrightness
            }
        }
    }

    var regeneratedBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(doc.rawOcrText) {
        regeneratedBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            doc.rawOcrText?.split(",")?.firstOrNull()?.let { data ->
                // Try QR first, then Barcode
                BarcodeGenerator.generateQRCode(data, 512, 512)?.asImageBitmap()
                    ?: BarcodeGenerator.generateBarcode(data, 1024, 300)?.asImageBitmap()
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        regeneratedBitmap?.let { bitmap ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.RadiusMedium))
                    .background(Color.White)
                    .padding(Dimens.SpacingMedium),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Regenerated Barcode",
                    modifier = Modifier.heightIn(max = 200.dp).fillMaxWidth(0.8f)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            Text("Regenerated Barcode", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
        }
    }
}

@Composable
fun TicketField(
    label: String, 
    value: String, 
    accent: DocAccent, 
    description: String = "$label $value", 
    alignment: Alignment.Horizontal = Alignment.Start,
    titleSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    onCopy: (String, String) -> Unit = { _, _ -> }
) {
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier
            .semantics { contentDescription = description }
            .clickable(
                onClick = {
                    clipboardManager.setText(AnnotatedString(value))
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCopy(label, value)
                }
            )
            .padding(4.dp),
        horizontalAlignment = alignment
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = titleSize),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
