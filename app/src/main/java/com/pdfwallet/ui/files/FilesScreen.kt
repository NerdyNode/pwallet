package com.pdfwallet.ui.files

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdfwallet.data.db.Document
import com.pdfwallet.ui.theme.Dimens
import com.pdfwallet.ui.theme.getDocAccent
import androidx.compose.foundation.isSystemInDarkTheme
import com.pdfwallet.ui.wallet.WalletViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    onNavigateToDocumentDetail: (Document) -> Unit
) {
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val selectedDocIds = remember { mutableStateListOf<Long>() }
    val isSelectionMode = selectedDocIds.isNotEmpty()
    
    val showDeleteDialog = remember { mutableStateOf(false) }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Delete Documents") },
            text = { Text("Are you sure you want to delete ${selectedDocIds.size} selected document(s)?") },
            confirmButton = {
                TextButton(onClick = {
                    selectedDocIds.forEach { viewModel.deleteDocument(it) }
                    selectedDocIds.clear()
                    showDeleteDialog.value = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedDocIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedDocIds.clear() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            showDeleteDialog.value = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("Files", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { /* TODO: Show sort options */ }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (documents.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(Dimens.ScreenPaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOff,
                    contentDescription = "No files",
                    modifier = Modifier.size(Dimens.IconExtraLarge),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                Text("No files yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                Text("Import a document to get started", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                Button(onClick = { /* TODO: Implement import */ }) {
                    Text("Import Document")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(documents, key = { it.id }) { doc ->
                    val isSelected = selectedDocIds.contains(doc.id)
                    FileListItem(
                        doc = doc,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onClick = {
                            if (isSelectionMode) {
                                if (isSelected) selectedDocIds.remove(doc.id) else selectedDocIds.add(doc.id)
                            } else {
                                onNavigateToDocumentDetail(doc)
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                selectedDocIds.add(doc.id)
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.ScreenPaddingLarge))
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    doc: Document,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val isDark = isSystemInDarkTheme()
    val accent = getDocAccent(doc.documentType, isDark)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = Dimens.ScreenPaddingLarge, vertical = Dimens.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.padding(end = Dimens.SpacingMedium)
            )
        } else {
            Surface(
                shape = CircleShape,
                color = accent.container,
                modifier = Modifier.size(Dimens.IconLarge)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = "File icon",
                        tint = accent.onContainer,
                        modifier = Modifier.size(Dimens.IconMedium)
                    )
                }
            }
            Spacer(modifier = Modifier.width(Dimens.SpacingMedium))
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = doc.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormat.format(Date(doc.importDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = doc.documentType.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
