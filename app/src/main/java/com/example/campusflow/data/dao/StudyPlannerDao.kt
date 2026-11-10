package com.example.campusflow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.campusflow.data.entities.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyPlannerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession)

    @Update
    suspend fun updateSession(session: StudySession)

    @Delete
    suspend fun deleteSession(session: StudySession)

    @Query("SELECT * FROM study_sessions WHERE id = :id")
    suspend fun getSessionById(id: Int): StudySession?

    @Query("SELECT * FROM study_sessions ORDER BY date DESC, startTime DESC")
    fun getAllSessions(): Flow<List<StudySession>>
}