package com.example.campusflow.data.repository

import com.example.campusflow.data.dao.SubjectDao
import com.example.campusflow.data.entities.Subject
import kotlinx.coroutines.flow.Flow

class SubjectRepository(private val subjectDao: SubjectDao) {
    fun getAllSubjectsStream(): Flow<List<Subject>> = subjectDao.getAllSubjects()
    
    suspend fun getSubjectStream(id: Int): Subject? = subjectDao.getSubjectById(id)
    
    suspend fun insertSubject(subject: Subject): Long = subjectDao.insertSubject(subject)
    
    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    
    suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)
}