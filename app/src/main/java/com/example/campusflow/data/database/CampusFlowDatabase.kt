package com.example.campusflow.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.campusflow.data.dao.AssignmentDao
import com.example.campusflow.data.dao.AttendanceDao
import com.example.campusflow.data.dao.NoteDao
import com.example.campusflow.data.dao.QuizDao
import com.example.campusflow.data.dao.StudyPlannerDao
import com.example.campusflow.data.dao.SubjectDao
import com.example.campusflow.data.dao.TaskDao
import com.example.campusflow.data.entities.Assignment
import com.example.campusflow.data.entities.AttendanceRecord
import com.example.campusflow.data.entities.Note
import com.example.campusflow.data.entities.QuizQuestion
import com.example.campusflow.data.entities.QuizResult
import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.data.entities.Subject
import com.example.campusflow.data.entities.Task

@Database(
    entities = [
        Task::class,
        Subject::class,
        Assignment::class,
        AttendanceRecord::class,
        Note::class,
        QuizQuestion::class,
        QuizResult::class,
        StudySession::class
    ],
    version = 1,
    exportSchema = false // Keeping false for simplicity, usually true for production to track migrations
)
@TypeConverters(Converters::class) // Apply the converters to the entire database
abstract class CampusFlowDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subjectDao(): SubjectDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun noteDao(): NoteDao
    abstract fun quizDao(): QuizDao
    abstract fun studyPlannerDao(): StudyPlannerDao

    companion object {
        @Volatile // Ensures that changes made by one thread are immediately visible to others
        private var Instance: CampusFlowDatabase? = null

        fun getDatabase(context: Context): CampusFlowDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CampusFlowDatabase::class.java,
                    "campus_flow_database"
                )
                    .fallbackToDestructiveMigration() // Simple approach: wipes DB on version change
                    .build()
                    .also { Instance = it }
            }
        }
    }
}