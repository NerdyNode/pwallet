package com.pdfwallet.ui.backup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfwallet.data.repository.SettingsRepository
import com.pdfwallet.service.sync.GoogleDriveSyncService
import com.pdfwallet.service.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class SyncState {
    IDLE, IN_PROGRESS, SUCCESS, ERROR
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val syncService: GoogleDriveSyncService,
    private val syncManager: SyncManager,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    val userEmail: StateFlow<String?> = settingsRepository.userEmailFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val lastBackupTime: StateFlow<Long> = settingsRepository.lastBackupTimeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    fun triggerManualSync() {
        syncManager.triggerManualSync()
        _syncState.value = SyncState.IN_PROGRESS
    }

    fun backupNow(email: String? = userEmail.value) {
        if (email == null) {
            _syncState.value = SyncState.ERROR
            _errorMessage.value = "No account selected. Please sign in again."
            return
        }
        viewModelScope.launch {
            _syncState.value = SyncState.IN_PROGRESS
            _errorMessage.value = null
            
            val dbFile = context.getDatabasePath("pdf_wallet.db")
            if (dbFile.exists()) {
                val result = syncService.backupDatabase(email, dbFile)
                result.onSuccess {
                    _syncState.value = SyncState.SUCCESS
                    settingsRepository.setLastBackupTime(System.currentTimeMillis())
                }.onFailure {
                    _syncState.value = SyncState.ERROR
                    _errorMessage.value = it.message
                }
            } else {
                _syncState.value = SyncState.ERROR
                _errorMessage.value = "Database file not found"
            }
        }
    }

    fun restoreNow(email: String? = userEmail.value) {
        if (email == null) {
            _syncState.value = SyncState.ERROR
            _errorMessage.value = "No account selected. Please sign in again."
            return
        }
        viewModelScope.launch {
            _syncState.value = SyncState.IN_PROGRESS
            _errorMessage.value = null
            
            val dbFile = context.getDatabasePath("pdf_wallet.db")
            val result = syncService.restoreDatabase(email, dbFile)
            result.onSuccess {
                _syncState.value = SyncState.SUCCESS
                // Note: Need to restart app or recreate database connection after restore
            }.onFailure {
                _syncState.value = SyncState.ERROR
                _errorMessage.value = it.message
            }
        }
    }
}
