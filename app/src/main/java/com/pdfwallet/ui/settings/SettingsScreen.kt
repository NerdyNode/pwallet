package com.pdfwallet.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdfwallet.BuildConfig
import com.pdfwallet.ui.theme.Dimens
import com.pdfwallet.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToDebug: () -> Unit = {}
) {
    val context = LocalContext.current
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val useDynamicColor by viewModel.useDynamicColor.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeModeFlow.collectAsStateWithLifecycle()
    val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()
    val backupStatus by viewModel.backupStatus.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(backupStatus) {
        backupStatus?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearBackupStatus()
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { viewModel.performBackup(context, it) }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.performRestore(context, it) }
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        SettingsContent(
            padding = padding,
            viewModel = viewModel,
            onNavigateToDebug = onNavigateToDebug
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    padding: PaddingValues,
    viewModel: SettingsViewModel,
    onNavigateToDebug: () -> Unit
) {
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle(initialValue = false)
    val themeMode by viewModel.themeModeFlow.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val useDynamicColor by viewModel.useDynamicColor.collectAsStateWithLifecycle(initialValue = true)
    val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle(initialValue = 0L)
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current
    
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let { viewModel.performBackup(context, it) }
    }
    
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.performRestore(context, it) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = Dimens.ScreenPaddingLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)
    ) {
        stickyHeader {
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = Dimens.SpacingSmall)
            )
        }
        item {
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
            val preventScreenshots by viewModel.preventScreenshots.collectAsStateWithLifecycle()

            SettingsSwitchItem(
                title = "Biometric Lock",
                subtitle = "Require fingerprint to open the app",
                icon = Icons.Default.Fingerprint,
                checked = isBiometricEnabled,
                onCheckedChange = { viewModel.setBiometricEnabled(it) }
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
            
            SettingsSwitchItem(
                title = "Prevent Screenshots",
                subtitle = "Block screenshots to protect sensitive IDs",
                icon = Icons.Default.Security,
                checked = preventScreenshots,
                onCheckedChange = { viewModel.togglePreventScreenshots(it) }
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
        }
        
        stickyHeader {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = Dimens.SpacingSmall)
            )
        }
        item {
            SettingsSwitchItem(
                title = "Dynamic Colors",
                subtitle = "Use system wallpaper colors",
                icon = Icons.Default.Palette,
                checked = useDynamicColor,
                onCheckedChange = { viewModel.setDynamicColorEnabled(it) }
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
            Text("Theme", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) { Text("System") }
                SegmentedButton(
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) { Text("Light") }
                SegmentedButton(
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) { Text("Dark") }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
        }

        stickyHeader {
            Text(
                text = "Data & Backup",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = Dimens.SpacingSmall)
            )
        }
        item {
            SettingsActionItem(
                title = "Backup to File",
                subtitle = if (lastBackupTime > 0) "Last backup: ${dateFormat.format(Date(lastBackupTime))}" else "Create a local backup",
                icon = Icons.Default.Save,
                onClick = { backupLauncher.launch("pdfwallet_backup.zip") }
            )
            
            SettingsActionItem(
                title = "Restore from File",
                subtitle = "Restore a previous backup",
                icon = Icons.Default.SettingsBackupRestore,
                onClick = { restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream")) }
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
        }
        
        stickyHeader {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = Dimens.SpacingSmall)
            )
        }
        item {
            SettingsActionItem(
                title = "Debug Logs",
                subtitle = "View app diagnostics",
                icon = Icons.Default.BugReport,
                onClick = onNavigateToDebug
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
            
            Text(
                text = "App Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.SpacingLarge),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpacingSmall),
        shape = RoundedCornerShape(Dimens.RadiusLarge)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(Dimens.SpacingNormal))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpacingSmall),
        shape = RoundedCornerShape(Dimens.RadiusLarge)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(Dimens.SpacingNormal))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Action for $title", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
