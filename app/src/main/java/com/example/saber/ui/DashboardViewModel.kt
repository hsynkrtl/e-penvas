package com.example.saber.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saber.data.NotebookEntity
import com.example.saber.data.NotesRepository
import com.example.saber.model.PaperStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val notebooks: List<NotebookEntity> = emptyList(),
    val folders: List<String> = emptyList(),
    val selectedFolder: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    private val _selectedFolder = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.allNotebooks,
        repository.allFolders,
        _selectedFolder,
        _searchQuery
    ) { notebooks, folders, selectedFolder, query ->
        val filtered = notebooks.filter { nb ->
            val matchesFolder = (selectedFolder == null || nb.folder == selectedFolder)
            val matchesQuery = (query.isBlank() || nb.title.contains(query, ignoreCase = true))
            matchesFolder && matchesQuery
        }
        DashboardUiState(
            notebooks = filtered,
            folders = folders,
            selectedFolder = selectedFolder,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSampleData()
        }
    }

    fun selectFolder(folder: String?) {
        _selectedFolder.value = folder
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createNotebook(
        title: String,
        folder: String,
        coverColor: Long,
        paperStyle: PaperStyle,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.createNotebook(
                title = title.ifBlank { "Unbenannte Notiz" },
                folder = folder.ifBlank { "Hauptordner" },
                coverColor = coverColor,
                paperStyle = paperStyle
            )
            onCreated(id)
        }
    }

    fun deleteNotebook(notebookId: Long) {
        viewModelScope.launch {
            repository.deleteNotebook(notebookId)
        }
    }

    fun updateNotebookTitle(notebook: NotebookEntity, newTitle: String) {
        viewModelScope.launch {
            repository.updateNotebook(notebook.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
        }
    }
}
