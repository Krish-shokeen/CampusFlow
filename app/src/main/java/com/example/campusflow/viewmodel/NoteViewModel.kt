package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.Note
import com.example.campusflow.data.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false
)

class NoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<NotesUiState> = combine(
        noteRepository.getAllNotesStream(),
        searchQuery
    ) { notes, query ->
        val filtered = if (query.isNotBlank()) {
            notes.filter { it.title.contains(query, true) || it.content.contains(query, true) }
        } else {
            notes
        }
        NotesUiState(notes = filtered.sortedByDescending { it.updatedAt }, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotesUiState(isLoading = true))

    fun addNote(note: Note) = viewModelScope.launch { noteRepository.insertNote(note) }
    fun updateNote(note: Note) = viewModelScope.launch { noteRepository.updateNote(note.copy(updatedAt = LocalDateTime.now())) }
    fun deleteNote(note: Note) = viewModelScope.launch { noteRepository.deleteNote(note) }
    suspend fun getNote(id: Int): Note? = noteRepository.getNoteStream(id)
}