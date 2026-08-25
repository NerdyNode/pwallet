package com.pdfwallet.ui.home

import android.content.Intent
import com.pdfwallet.ui.add.AddDocumentSheet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.ui.theme.Dimens
import com.pdfwallet.ui.wallet.DocumentCard
import com.pdfwallet.ui.wallet.WalletViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateToWallet: () -> Unit,
    onNavigateToDebugLogs: () -> Unit,
    onNavigateToDocumentDetail: (Document) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToTab: (String) -> Unit = {},
    viewModel: WalletViewModel = hiltViewModel()
) {
    val documents = viewModel.pagedDocuments.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val expiringDocs by viewModel.expiringDocuments.collectAsStateWithLifecycle()
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()

    var showAddSheet by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Wallet,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpacingMedium))
                        Text(
                            text = "PdfWallet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePrivacyMode() }) {
                        Icon(
                            if (privacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Privacy Mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { /* TODO Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { /* Profile */ }) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (documents.itemCount > 0) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.padding(Dimens.SpacingNormal)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Document", modifier = Modifier.size(28.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { 
                scope.launch {
                    isRefreshing = true
                    viewModel.refresh()
                    kotlinx.coroutines.delay(500)
                    isRefreshing = false 
                }
            },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Bar
                var searchExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.padding(horizontal = Dimens.ScreenPaddingLarge, vertical = Dimens.SpacingSmall)) {
                    SearchBar(
                        modifier = Modifier.fillMaxWidth(),
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { viewModel.searchQuery.value = it },
                            onSearch = { searchExpanded = false },
                            expanded = searchExpanded,
                            onExpandedChange = { searchExpanded = it },
                            placeholder = { Text("Search documents...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingIcon = {
                                IconButton(onClick = { /* TODO Filters */ }) {
                                    Icon(Icons.Default.Tune, contentDescription = "Filters", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                    },
                    expanded = searchExpanded,
                    onExpandedChange = { searchExpanded = it },
                    shape = RoundedCornerShape(Dimens.RadiusExtraLarge),
                    colors = SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) { }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))

            // Category Filter Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = Dimens.ScreenPaddingLarge),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)
            ) {
                item {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { viewModel.selectedCategory.value = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(Dimens.RadiusFull),
                        border = null
                    )
                }
                items(com.pdfwallet.data.db.DocumentCategory.entries) { category ->
                    FilterChip(
                        selected = selectedType == category,
                        onClick = { viewModel.selectedCategory.value = category },
                        label = { Text(category.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(Dimens.RadiusFull),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))

            if (expiringDocs.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ScreenPaddingLarge)
                        .padding(bottom = Dimens.SpacingMedium),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.SpacingMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Expiring Soon",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpacingMedium))
                        Text(
                            text = "${expiringDocs.size} documents expiring soon!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (documents.itemCount == 0) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenPaddingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpacingExtraLarge))
                    Text(
                        text = "Your wallet is empty",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                    Text(
                        text = "Import your first document to get started\nSupported: PDF, Images, PAN, DL, Tickets & more",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingExtraLarge))
                    
                    Button(
                        onClick = { showAddSheet = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(Dimens.RadiusFull)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Document", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val windowSizeClass = androidx.compose.material3.adaptive.currentWindowAdaptiveInfo().windowSizeClass
                val isExpanded = windowSizeClass.windowWidthSizeClass == androidx.window.core.layout.WindowWidthSizeClass.EXPANDED
                val isMedium = windowSizeClass.windowWidthSizeClass == androidx.window.core.layout.WindowWidthSizeClass.MEDIUM
                val columnCount = when {
                    isExpanded -> 3
                    isMedium -> 2
                    else -> 1
                }

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(columnCount),
                    contentPadding = PaddingValues(horizontal = Dimens.ScreenPaddingLarge, vertical = Dimens.SpacingMedium),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingNormal),
                    verticalItemSpacing = Dimens.SpacingNormal,
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        count = documents.itemCount,
                        key = { index -> documents[index]?.id ?: index }
                    ) { index ->
                        val doc = documents[index]
                        if (doc != null) {
                            with(sharedTransitionScope) {
                                DocumentCard(
                                    doc = doc,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    privacyMode = privacyMode,
                                    onClick = { onNavigateToDocumentDetail(doc) },
                                    onRetry = { viewModel.retryProcessing(doc.id) }
                                )
                            }
                        }
                    }
                    
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(modifier = Modifier.height(100.dp)) // Padding for FAB
                    }
                }
            }
        }
        }
    }
    
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { viewModel.importManualDocument(it) }
    }

    AddDocumentSheet(
        show = showAddSheet,
        onDismiss = { showAddSheet = false },
        onCameraClick = {
            onNavigateToCamera()
        },
        onGalleryClick = {
            pdfPickerLauncher.launch("image/*")
        },
        onPdfClick = {
            pdfPickerLauncher.launch("application/pdf")
        },
        onDriveClick = {
            onNavigateToTab("settings_tab")
        }
    )
}
