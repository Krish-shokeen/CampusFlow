package com.example.campusflow.di

import android.content.Context
import com.example.campusflow.data.database.CampusFlowDatabase
import com.example.campusflow.data.repository.AssignmentRepository
import com.example.campusflow.data.repository.AttendanceRepository
import com.example.campusflow.data.repository.NoteRepository
import com.example.campusflow.data.repository.QuizRepository
import com.example.campusflow.data.repository.StudyPlannerRepository
import com.example.campusflow.data.repository.SubjectRepository
import com.example.campusflow.data.repository.TaskRepository

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val taskRepository: TaskRepository
    val subjectRepository: SubjectRepository
    val assignmentRepository: AssignmentRepository
    val attendanceRepository: AttendanceRepository
    val noteRepository: NoteRepository
    val quizRepository: QuizRepository
    val studyPlannerRepository: StudyPlannerRepository
}

/**
 * Implementation for the Dependency Injection container at the application level.
 * Variables are initialized lazily so they are only created when accessed.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {
    
    // Create the DB instance lazily
    private val database: CampusFlowDatabase by lazy {
        CampusFlowDatabase.getDatabase(context)
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }
    
    override val subjectRepository: SubjectRepository by lazy {
        SubjectRepository(database.subjectDao())
    }
    
    override val assignmentRepository: AssignmentRepository by lazy {
        AssignmentRepository(database.assignmentDao())
    }
    
    override val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepository(database.attendanceDao())
    }
    
    override val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }
    
    override val quizRepository: QuizRepository by lazy {
        QuizRepository(database.quizDao())
    }
    
    override val studyPlannerRepository: StudyPlannerRepository by lazy {
        StudyPlannerRepository(database.studyPlannerDao())
    }
}