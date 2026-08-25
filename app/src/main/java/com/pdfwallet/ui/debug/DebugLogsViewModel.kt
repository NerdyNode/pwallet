package com.pdfwallet.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfwallet.data.db.AppLog
import com.pdfwallet.data.db.AppLogDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugLogsViewModel @Inject constructor(
    private val appLogDao: AppLogDao
) : ViewModel() {

    val logs: Flow<List<AppLog>> = appLogDao.getAllLogs()

    fun clearLogs() {
        viewModelScope.launch {
            appLogDao.clearAll()
        }
    }
}
