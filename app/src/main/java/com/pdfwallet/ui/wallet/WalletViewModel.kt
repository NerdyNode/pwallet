package com.pdfwallet.ui.wallet

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfwallet.data.db.CaptureSource
import com.pdfwallet.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.data.db.DocumentCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<DocumentCategory?>(null)
    val sortOrder = MutableStateFlow("DATE_ADDED")
    val privacyMode = MutableStateFlow(true) // Privacy on by default

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val documents: StateFlow<List<Document>> = combine(
        searchQuery,
        selectedCategory,
        sortOrder
    ) { query, category, sort ->
        Triple(query, category, sort)
    }.flatMapLatest { (query, category, sort) ->
        val types = category?.types ?: emptyList()
        repository.searchDocuments(query, types, sort)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pagedDocuments = repository.getPagedDocuments().cachedIn(viewModelScope)

    val expiringDocuments: StateFlow<List<Document>> = repository.getDocumentsExpiringBefore(
        System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun retryProcessing(docId: Long) {
        viewModelScope.launch {
            repository.retryProcessing(docId)
        }
    }

    fun importManualDocument(uri: Uri) {
        viewModelScope.launch {
            repository.captureDocument(uri, CaptureSource.MANUAL_IMPORT)
        }
    }

    fun togglePrivacyMode() {
        privacyMode.value = !privacyMode.value
    }
    
    fun deleteDocument(id: Long) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }

    fun refresh() {
        // Triggered by pull-to-refresh
    }
}
