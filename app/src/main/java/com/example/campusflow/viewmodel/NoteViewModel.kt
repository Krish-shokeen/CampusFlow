package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.Note
import com.example.campusflow.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = false
)

class NoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    val searchQuery = MutableStateFlow("")
    val categoryFilter = MutableStateFlow<String?>(null)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val uiState: StateFlow<NotesUiState> = combine(
        noteRepository.getAllNotesStream(),
        searchQuery,
        categoryFilter
    ) { notes, query, category ->
        val categories = notes.map { it.category }.distinct().sorted()
        var filtered = notes
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, true) || it.content.contains(query, true)
            }
        }
        if (category != null) {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }
        val sorted = filtered.sortedWith(
            compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt }
        )
        NotesUiState(notes = sorted, categories = categories, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotesUiState(isLoading = true))

    fun addNote(note: Note) = viewModelScope.launch {
        runCatching { noteRepository.insertNote(note) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to create note." }
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        runCatching { noteRepository.updateNote(note.copy(updatedAt = LocalDateTime.now())) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to update note." }
    }

    fun togglePin(note: Note) = viewModelScope.launch {
        runCatching { noteRepository.togglePin(note) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to update note pin." }
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        runCatching { noteRepository.deleteNote(note) }
            .onFailure { _errorMessage.value = it.message ?: "Failed to delete note." }
    }

    suspend fun getNote(id: Int): Note? = noteRepository.getNoteStream(id)

    fun clearError() {
        _errorMessage.value = null
    }
}
