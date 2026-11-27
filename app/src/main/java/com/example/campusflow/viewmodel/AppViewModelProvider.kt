package com.example.campusflow.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.campusflow.CampusFlowApplication

/**
 * Provides Factory to create instances of ViewModels for the entire app.
 * This replaces Hilt/Dagger by manually injecting dependencies from the AppContainer.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            TaskViewModel(campusFlowApplication().container.taskRepository)
        }
        initializer {
            AssignmentViewModel(
                campusFlowApplication().container.assignmentRepository,
                campusFlowApplication().container.subjectRepository
            )
        }
        initializer {
            AttendanceViewModel(
                campusFlowApplication().container.attendanceRepository,
                campusFlowApplication().container.subjectRepository
            )
        }
        // Initializer for QuizViewModel
        initializer {
            QuizViewModel(campusFlowApplication().container.quizRepository)
        }
<<<<<<< Updated upstream
        initializer {
            ProfileSettingsViewModel(campusFlowApplication().container.userPreferencesRepository)
        }
        initializer {
            NotesViewModel(
                campusFlowApplication().container.noteRepository,
                campusFlowApplication().container.subjectRepository
            )
        }
        initializer {
            StudyPlannerViewModel(
                campusFlowApplication().container.studyPlannerRepository,
                campusFlowApplication().container.subjectRepository
            )
=======
        initializer { NoteViewModel(campusFlowApplication().container.noteRepository) }
        initializer { StudyPlannerViewModel(campusFlowApplication().container.studyPlannerRepository, campusFlowApplication().container.subjectRepository) }
        initializer { ProfileViewModel(campusFlowApplication().container.userPreferencesRepository) }
        initializer { 
            val container = campusFlowApplication().container
            StatisticsViewModel(
                container.taskRepository, 
                container.assignmentRepository, 
                container.studyPlannerRepository, 
                container.quizRepository,
                container.attendanceRepository
            ) 
>>>>>>> Stashed changes
        }
    }
}

/**
 * Extension function to quickly get the application instance from CreationExtras.
 */
fun CreationExtras.campusFlowApplication(): CampusFlowApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CampusFlowApplication)