package com.example.campusflow.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.campusflow.data.database.CampusFlowDatabase
import com.example.campusflow.data.preferences.UserPreferencesRepository
import com.example.campusflow.data.repository.AssignmentRepository
import com.example.campusflow.data.repository.AttendanceRepository
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.data.repository.NoteRepository
import com.example.campusflow.data.repository.QuizRepository
import com.example.campusflow.data.repository.StatisticsRepository
import com.example.campusflow.data.repository.StudyPlannerRepository
import com.example.campusflow.data.repository.SubjectRepository
import com.example.campusflow.data.repository.TaskRepository
import com.example.campusflow.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.storage.FirebaseStorage

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

interface AppContainer {
    val authRepository: AuthRepository
    val userRepository: UserRepository
    val taskRepository: TaskRepository
    val subjectRepository: SubjectRepository
    val assignmentRepository: AssignmentRepository
    val attendanceRepository: AttendanceRepository
    val noteRepository: NoteRepository
    val quizRepository: QuizRepository
    val studyPlannerRepository: StudyPlannerRepository
    val statisticsRepository: StatisticsRepository
    val userPreferencesRepository: UserPreferencesRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: CampusFlowDatabase by lazy { CampusFlowDatabase.getDatabase(context) }

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
        }
    }

    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.dataStore)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth, firestore)
    }

    override val userRepository: UserRepository by lazy {
        UserRepository(authRepository, firestore, storage, userPreferencesRepository)
    }

    override val subjectRepository: SubjectRepository by lazy {
        SubjectRepository(authRepository, firestore)
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepository(authRepository, firestore)
    }

    override val assignmentRepository: AssignmentRepository by lazy {
        AssignmentRepository(authRepository, subjectRepository, firestore)
    }

    override val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepository(authRepository, subjectRepository, firestore)
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepository(authRepository, subjectRepository, firestore)
    }

    override val quizRepository: QuizRepository by lazy {
        QuizRepository(database.quizDao(), authRepository, firestore)
    }

    override val studyPlannerRepository: StudyPlannerRepository by lazy {
        StudyPlannerRepository(authRepository, subjectRepository, firestore)
    }

    override val statisticsRepository: StatisticsRepository by lazy {
        StatisticsRepository(
            taskRepository = taskRepository,
            assignmentRepository = assignmentRepository,
            attendanceRepository = attendanceRepository,
            quizRepository = quizRepository,
            studyPlannerRepository = studyPlannerRepository
        )
    }
}
