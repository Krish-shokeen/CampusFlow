package com.example.campusflow.data.repository

import com.example.campusflow.data.dao.AttendanceDao
import com.example.campusflow.data.entities.AttendanceRecord
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val attendanceDao: AttendanceDao) {
    fun getAllRecordsStream(): Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    
    fun getRecordsBySubjectStream(subjectId: Int): Flow<List<AttendanceRecord>> = 
        attendanceDao.getRecordsBySubject(subjectId)
    
    suspend fun getRecordStream(id: Int): AttendanceRecord? = attendanceDao.getRecordById(id)
    
    suspend fun insertRecord(record: AttendanceRecord) = attendanceDao.insertRecord(record)
    
    suspend fun updateRecord(record: AttendanceRecord) = attendanceDao.updateRecord(record)
    
    suspend fun deleteRecord(record: AttendanceRecord) = attendanceDao.deleteRecord(record)
}