package com.example.campusflow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.campusflow.data.entities.AttendanceRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord)

    @Update
    suspend fun updateRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)

    @Query("SELECT * FROM attendance_records WHERE id = :id")
    suspend fun getRecordById(id: Int): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getRecordsBySubject(subjectId: Int): Flow<List<AttendanceRecord>>
    
    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>
}