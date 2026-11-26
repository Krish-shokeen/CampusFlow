package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.entities.Note
import com.example.campusflow.data.entities.Subject
import com.example.campusflow.data.repository.NoteRepository
import com.example.campusflow.data.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = true
)

class NotesViewModel(
    private val noteRepository: NoteRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {
    val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<NotesUiState> = combine(
        noteRepository.getAllNotesStream(),
        subjectRepository.getAllSubjectsStream(),
        searchQuery
    ) { notes, subjects, query ->
        NotesUiState(
            notes = notes.filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
            },
            subjects = subjects,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun saveNote(note: Note) {
        viewModelScope.launch {
            if (note.id == 0) noteRepository.insertNote(note)
            else noteRepository.updateNote(note.copy(updatedAt = LocalDateTime.now()))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { noteRepository.deleteNote(note) }
    }
}
