package com.example.campusflow.data.repository

import com.example.campusflow.data.dao.StudyPlannerDao
import com.example.campusflow.data.entities.StudySession
import kotlinx.coroutines.flow.Flow

class StudyPlannerRepository(private val studyPlannerDao: StudyPlannerDao) {
    fun getAllSessionsStream(): Flow<List<StudySession>> = studyPlannerDao.getAllSessions()
    
    suspend fun getSessionStream(id: Int): StudySession? = studyPlannerDao.getSessionById(id)
    
    suspend fun insertSession(session: StudySession) = studyPlannerDao.insertSession(session)
    
    suspend fun updateSession(session: StudySession) = studyPlannerDao.updateSession(session)
    
    suspend fun deleteSession(session: StudySession) = studyPlannerDao.deleteSession(session)
}