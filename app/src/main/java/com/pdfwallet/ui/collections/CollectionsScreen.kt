package com.pdfwallet.ui.collections

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdfwallet.ui.theme.getDocAccent

import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pdfwallet.data.db.DocumentCategory
import com.pdfwallet.ui.theme.Dimens
import com.pdfwallet.ui.wallet.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit = {}
) {
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    
    // Group documents by category
    val collections = remember(documents) {
        DocumentCategory.entries.associateWith { category ->
            documents.filter { doc -> category.types.contains(doc.documentType) }
        }.filterValues { it.isNotEmpty() }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collections", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (collections.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                    text = "No collections found",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                Text(
                    text = "Collections will appear here once you add documents of various categories.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingExtraLarge))
                Button(
                    onClick = onNavigateToHome,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(collections.toList(), key = { it.first.name }) { (category, docs) ->
                    CollectionListItem(
                        category = category,
                        count = docs.size,
                        onClick = {
                            viewModel.selectedCategory.value = category
                            onNavigateToHome()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.ScreenPaddingLarge))
                }
            }
        }
    }
}

@Composable
fun CollectionListItem(category: DocumentCategory, count: Int, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val documentType = category.types.firstOrNull()
    val accent = if (documentType != null) getDocAccent(documentType, isDark) else getDocAccent(com.pdfwallet.data.db.DocumentType.OTHER, isDark)

    val icon = when (category.name) {
        "TRAVEL" -> Icons.Default.FlightTakeoff
        "HOTEL" -> Icons.Default.Hotel
        "ID_PROOF" -> Icons.Default.Badge
        "MEMBERSHIPS" -> Icons.Default.CardMembership
        "TRANSIT" -> Icons.Default.DirectionsBus
        else -> Icons.Default.Folder
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.ScreenPaddingLarge, vertical = Dimens.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = accent.container,
            contentColor = accent.onContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(Dimens.SpacingMedium))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count documents",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(Dimens.SpacingMedium))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "View Collection",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
