package com.pdfwallet.ui.detail

import android.content.Intent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdfwallet.data.db.Document
import com.pdfwallet.ui.theme.Dimens
import com.pdfwallet.ui.theme.DocAccent
import com.pdfwallet.ui.theme.getDocAccent
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.DocumentDetailScreen(
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Document") },
            text = { Text("This document will be permanently deleted. This action cannot be undone.") },
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
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
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

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpacingNormal),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DocumentContent(doc, accent, animatedVisibilityScope) { label, value ->
                    scope.launch {
                        snackbarHostState.showSnackbar("$label copied")
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
fun SharedTransitionScope.DocumentContent(
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
        shape = RoundedCornerShape(16.dp),
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

            Column(modifier = Modifier.padding(Dimens.SpacingLarge)) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val addedDate = dateFormat.format(Date(doc.importDate))
                val meta = doc.metadata

                when (meta) {
                    is com.pdfwallet.data.db.DocumentMetadata.GovernmentId -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("ID NUMBER", doc.documentId ?: "--", accent, onCopy)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("HOLDER NAME", doc.holderName ?: "--", accent, onCopy)
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("DOB", meta.dateOfBirth ?: "--", accent, onCopy)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("FATHER/GUARDIAN", meta.fatherOrGuardianName ?: "--", accent, onCopy)
                            }
                        }
                    }
                    is com.pdfwallet.data.db.DocumentMetadata.Hotel -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("HOTEL", meta.hotelName ?: "--", accent, onCopy)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("GUEST", doc.holderName ?: "--", accent, onCopy)
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("CHECK-IN", meta.checkIn ?: "--", accent, onCopy)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("CHECK-OUT", meta.checkOut ?: "--", accent, onCopy)
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                        DocumentField("ROOM DETAILS", meta.roomDetails ?: "--", accent, onCopy)
                    }
                    is com.pdfwallet.data.db.DocumentMetadata.Membership -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("PROVIDER", meta.provider ?: "--", accent, onCopy)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("MEMBER", meta.memberName ?: "--", accent, onCopy)
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("MEMBER ID", doc.documentId ?: "--", accent, onCopy)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("VALIDITY", meta.validity ?: "--", accent, onCopy)
                            }
                        }
                    }
                    else -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("DOCUMENT ID", doc.documentId ?: "--", accent, onCopy)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentField("ADDED ON", addedDate, accent, onCopy)
                            }
                        }
                        if (doc.expiryDate != null) {
                            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                            DocumentField("EXPIRES ON", doc.expiryDate, accent, onCopy)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
            
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
fun DocumentField(
    label: String, 
    value: String, 
    accent: DocAccent,
    onCopy: (String, String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                clipboardManager.setText(AnnotatedString(value))
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCopy(label, value)
            }
            .padding(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
