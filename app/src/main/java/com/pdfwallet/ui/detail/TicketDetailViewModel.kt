package com.pdfwallet.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfwallet.data.db.Document
import com.pdfwallet.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketDetailViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _document = MutableStateFlow<Document?>(null)
    val document: StateFlow<Document?> = _document.asStateFlow()

    fun loadDocument(id: Long) {
        viewModelScope.launch {
            _document.value = repository.getDocumentById(id)
        }
    }

    fun updateDocument(doc: Document) {
        viewModelScope.launch {
            repository.updateDocument(doc)
            _document.value = doc
        }
    }

    fun deleteDocument(onDeleted: () -> Unit) {
        viewModelScope.launch {
            document.value?.let { doc ->
                repository.deleteDocument(doc.id)
                onDeleted()
            }
        }
    }
}
