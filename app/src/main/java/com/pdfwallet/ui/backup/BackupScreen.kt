package com.pdfwallet.ui.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.pdfwallet.ui.theme.Dimens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()

    val credentialManager = remember { CredentialManager.create(context) }

    suspend fun getGoogleEmail(): String? {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            googleIdTokenCredential.id
        } catch (e: Exception) {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .padding(Dimens.ScreenPaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val icon = when (syncState) {
                    SyncState.SUCCESS, SyncState.IDLE -> Icons.Default.CloudDone
                    SyncState.ERROR -> Icons.Default.Error
                    SyncState.IN_PROGRESS -> Icons.Default.CloudUpload
                }
                
                val tint = when (syncState) {
                    SyncState.SUCCESS, SyncState.IDLE -> MaterialTheme.colorScheme.primary
                    SyncState.ERROR -> MaterialTheme.colorScheme.error
                    SyncState.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "Backup Status",
                    modifier = Modifier.size(Dimens.IconHero),
                    tint = tint
                )
                
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                
                if (syncState == SyncState.IN_PROGRESS) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                    Text("Syncing with Google Drive...", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(
                        text = if (syncState == SyncState.ERROR) "Backup Failed" else "Backup is up to date",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                    
                    if (syncState == SyncState.ERROR) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        val timeStr = lastBackupTime?.let {
                            val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                            sdf.format(Date(it))
                        } ?: "Never"
                        
                        Text(
                            text = "Last backup: $timeStr",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Dimens.SpacingExtraLarge))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)) {
                        Button(
                            onClick = {
                                if (userEmail != null) {
                                    viewModel.backupNow()
                                } else {
                                    scope.launch {
                                        val email = getGoogleEmail()
                                        if (email != null) {
                                            viewModel.backupNow(email)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Backup Now")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                if (userEmail != null) {
                                    viewModel.restoreNow()
                                } else {
                                    scope.launch {
                                        val email = getGoogleEmail()
                                        if (email != null) {
                                            viewModel.restoreNow(email)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restore")
                        }
                    }
                }
            }
        }
    }
}

// TODO: Move this to BuildConfig.WEB_CLIENT_ID in build.gradle.kts
private val WEB_CLIENT_ID = com.pdfwallet.BuildConfig.WEB_CLIENT_ID
