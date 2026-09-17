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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DateRange
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
import com.pdfwallet.data.db.DocumentMetadata
import com.pdfwallet.ui.theme.Dimens
import com.pdfwallet.ui.theme.DocAccent
import com.pdfwallet.ui.theme.getDocAccent
import com.pdfwallet.util.BarcodeGenerator
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import android.content.Intent
import java.io.File

private fun mimeTypeForFile(filePath: String): String = when {
    filePath.endsWith(".pdf", true) -> "application/pdf"
    filePath.endsWith(".jpg", true) || filePath.endsWith(".jpeg", true) -> "image/jpeg"
    filePath.endsWith(".png", true) -> "image/png"
    filePath.endsWith(".webp", true) -> "image/webp"
    else -> "*/*"
}

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
    var showEditSheet by remember { mutableStateOf(false) }

    if (showEditSheet && document != null) {
        TicketEditBottomSheet(
            document = document!!,
            onDismiss = { showEditSheet = false },
            onSave = { updatedDoc ->
                // Assuming viewModel has an updateDocument method. If not we will add it.
                viewModel.updateDocument(updatedDoc)
            }
        )
    }

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
            CenterAlignedTopAppBar(
                title = { Text(document?.title ?: "Document Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                actions = {
                    document?.let { doc ->
                        // Only show for event/airline/train
                        val hasDates = doc.documentType == com.pdfwallet.data.db.DocumentType.AIRLINE ||
                                       doc.documentType == com.pdfwallet.data.db.DocumentType.TRAIN ||
                                       doc.documentType == com.pdfwallet.data.db.DocumentType.BUS ||
                                       doc.documentType == com.pdfwallet.data.db.DocumentType.EVENT ||
                                       doc.documentType == com.pdfwallet.data.db.DocumentType.MOVIE
                        if (hasDates) {
                            IconButton(onClick = { 
                                val intent = android.content.Intent(android.content.Intent.ACTION_INSERT)
                                    .setData(android.provider.CalendarContract.Events.CONTENT_URI)
                                    .putExtra(android.provider.CalendarContract.Events.TITLE, doc.title)
                                
                                doc.journeyDate?.let { date ->
                                    intent.putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, date)
                                    intent.putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, date + (2 * 60 * 60 * 1000)) // 2 hours default
                                }
                                
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Ignore if no calendar app
                                }
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Add to Calendar")
                            }
                        }
                    }
                    IconButton(onClick = { 
                        showEditSheet = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Document")
                    }
                    IconButton(onClick = { 
                        showDeleteConfirm = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Document", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                                    type = mimeTypeForFile(doc.filePath)
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Document"))
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
                                val mime = mimeTypeForFile(doc.filePath)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, mime)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = mime
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(fallbackIntent, "Save or View"))
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(Dimens.RadiusFull)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("View / Download")
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
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Original Document", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!doc.thumbnailPath.isNullOrEmpty()) {
                        val file = java.io.File(doc.thumbnailPath)
                        if (file.exists()) {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                java.io.File(doc.filePath)
                            )
                            androidx.compose.foundation.Image(
                                painter = coil.compose.rememberAsyncImagePainter(file),
                                contentDescription = "Original Document",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        val mime = mimeTypeForFile(doc.filePath)
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, mime)
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch(e: Exception) {
                                            val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = mime
                                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(android.content.Intent.createChooser(fallbackIntent, "Save or View"))
                                        }
                                    },
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
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
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            com.pdfwallet.ui.pass.PassCard(
                document = doc,
                onCopy = onCopy,
                barcodeContent = { BarcodeSection(doc, accent) }
            )
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

    var barcodeBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isOriginal by remember { mutableStateOf(false) }

    LaunchedEffect(doc.contentHash, doc.documentId, doc.documentType) {
        barcodeBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // Priority 1: Load original barcode image extracted from the document
            val barcodesDir = java.io.File(context.filesDir, "barcodes")
            val savedFile = java.io.File(barcodesDir, "${doc.contentHash}.png")
            if (savedFile.exists()) {
                val bmp = android.graphics.BitmapFactory.decodeFile(savedFile.absolutePath)
                if (bmp != null) {
                    isOriginal = true
                    return@withContext bmp.asImageBitmap()
                }
            }

            // Priority 2: Fallback — regenerate barcode from documentId
            isOriginal = false
            val data = doc.documentId
            if (!data.isNullOrEmpty()) {
                when (doc.documentType) {
                    com.pdfwallet.data.db.DocumentType.AIRLINE, com.pdfwallet.data.db.DocumentType.TRAIN -> {
                        BarcodeGenerator.generatePDF417(data, 1024, 300)?.asImageBitmap()
                            ?: BarcodeGenerator.generateQRCode(data, 512, 512)?.asImageBitmap()
                    }
                    com.pdfwallet.data.db.DocumentType.MOVIE, com.pdfwallet.data.db.DocumentType.HOTEL -> {
                        BarcodeGenerator.generateQRCode(data, 512, 512)?.asImageBitmap()
                    }
                    else -> {
                        BarcodeGenerator.generateBarcode(data, 1024, 300)?.asImageBitmap()
                            ?: BarcodeGenerator.generateQRCode(data, 512, 512)?.asImageBitmap()
                    }
                }
            } else null
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        barcodeBitmap?.let { bitmap ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.RadiusMedium))
                    .background(Color.White)
                    .padding(Dimens.SpacingMedium),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = if (isOriginal) "Original Barcode" else "Barcode",
                    modifier = Modifier.heightIn(max = 200.dp).fillMaxWidth(0.8f)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            Text(
                if (isOriginal) "Original Barcode" else "Regenerated Barcode",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
