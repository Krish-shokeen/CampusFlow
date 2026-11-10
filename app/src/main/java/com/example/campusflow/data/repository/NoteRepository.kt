package com.example.campusflow.data.repository

import com.example.campusflow.data.dao.NoteDao
import com.example.campusflow.data.entities.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    fun getAllNotesStream(): Flow<List<Note>> = noteDao.getAllNotes()
    
    fun searchNotesStream(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    
    suspend fun getNoteStream(id: Int): Note? = noteDao.getNoteById(id)
    
    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
}