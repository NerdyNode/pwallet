package com.pdfwallet.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfwallet.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val backupRestoreManager: com.pdfwallet.data.local.BackupRestoreManager
) : ViewModel() {

    val isBiometricEnabled: StateFlow<Boolean> = settingsRepository.biometricEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val useDynamicColor: StateFlow<Boolean> = settingsRepository.useDynamicColorFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val themeModeFlow: StateFlow<com.pdfwallet.ui.theme.ThemeMode> = settingsRepository.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.pdfwallet.ui.theme.ThemeMode.SYSTEM
        )

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDynamicColor(enabled)
        }
    }

    fun setThemeMode(mode: com.pdfwallet.ui.theme.ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    val lastBackupTime: StateFlow<Long> = settingsRepository.lastBackupTimeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricEnabled(enabled)
        }
    }

    private val _backupStatus = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus

    fun performBackup(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _backupStatus.value = "Backing up..."
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val success = backupRestoreManager.backup(outputStream)
                    if (success) {
                        settingsRepository.setLastBackupTime(System.currentTimeMillis())
                        _backupStatus.value = "Backup successful!"
                    } else {
                        _backupStatus.value = "Backup failed."
                    }
                } ?: run {
                    _backupStatus.value = "Could not create backup file."
                }
            } catch (e: Exception) {
                _backupStatus.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun performRestore(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _backupStatus.value = "Restoring..."
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val result = backupRestoreManager.restore(inputStream)
                    if (result.isSuccess) {
                        _backupStatus.value = "Restored ${result.pdfsRestored} documents. Please restart app."
                    } else {
                        _backupStatus.value = "Restore failed."
                    }
                } ?: run {
                    _backupStatus.value = "Could not open backup file."
                }
            } catch (e: Exception) {
                _backupStatus.value = "Error: ${e.localizedMessage}"
            }
        }
    }
    
    fun clearBackupStatus() {
        _backupStatus.value = null
    }
}
