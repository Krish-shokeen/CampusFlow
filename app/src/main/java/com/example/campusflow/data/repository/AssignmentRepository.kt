package com.example.campusflow.data.repository

import com.example.campusflow.data.dao.AssignmentDao
import com.example.campusflow.data.entities.Assignment
import kotlinx.coroutines.flow.Flow

class AssignmentRepository(private val assignmentDao: AssignmentDao) {
    fun getAllAssignmentsStream(): Flow<List<Assignment>> = assignmentDao.getAllAssignments()
    
    fun getAssignmentsBySubjectStream(subjectId: Int): Flow<List<Assignment>> = 
        assignmentDao.getAssignmentsBySubject(subjectId)
    
    suspend fun getAssignmentStream(id: Int): Assignment? = assignmentDao.getAssignmentById(id)
    
    suspend fun insertAssignment(assignment: Assignment) = assignmentDao.insertAssignment(assignment)
    
    suspend fun updateAssignment(assignment: Assignment) = assignmentDao.updateAssignment(assignment)
    
    suspend fun deleteAssignment(assignment: Assignment) = assignmentDao.deleteAssignment(assignment)
}