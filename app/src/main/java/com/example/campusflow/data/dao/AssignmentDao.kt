package com.example.campusflow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.campusflow.data.entities.Assignment
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment)

    @Update
    suspend fun updateAssignment(assignment: Assignment)

    @Delete
    suspend fun deleteAssignment(assignment: Assignment)

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getAssignmentById(id: Int): Assignment?

    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE subjectId = :subjectId ORDER BY dueDate ASC")
    fun getAssignmentsBySubject(subjectId: Int): Flow<List<Assignment>>
}