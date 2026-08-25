package com.pdfwallet.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfwallet.data.db.DocumentTypeCount
import com.pdfwallet.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: DocumentRepository
) : ViewModel() {

    val documentTypeCounts: StateFlow<List<DocumentTypeCount>> = repository.documentTypeCounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expiringSoonCount: StateFlow<Int> = repository.getDocumentsExpiringBefore(
        System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
    ).map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _selectedRange = kotlinx.coroutines.flow.MutableStateFlow("This Month")
    val selectedRange: StateFlow<String> = _selectedRange

    fun onRangeSelected(range: String) {
        _selectedRange.value = range
        // TODO: Update filtered data based on range
    }

    // Placeholders for storage and trend
    val storageUsed = kotlinx.coroutines.flow.MutableStateFlow("12.4 MB")
    val trendData = kotlinx.coroutines.flow.MutableStateFlow(listOf(10f, 25f, 15f, 35f, 28f, 40f, 55f))
}
