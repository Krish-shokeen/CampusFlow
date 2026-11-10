package com.example.campusflow.data.seeder

import com.example.campusflow.data.entities.Assignment
import com.example.campusflow.data.entities.AttendanceRecord
import com.example.campusflow.data.entities.Note
import com.example.campusflow.data.entities.Priority
import com.example.campusflow.data.entities.Subject
import com.example.campusflow.data.entities.Task
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

object DatabaseSeeder {

    fun seedDatabase(container: AppContainer) {
        CoroutineScope(Dispatchers.IO).launch {
            val existingSubjects = container.subjectRepository.getAllSubjectsStream().first()
            val existingQuestions = container.quizRepository.getQuizCategoriesStream().first()
            
            // Seed Quiz Questions if none exist
            if (existingQuestions.isEmpty()) {
                container.quizRepository.insertQuestions(QuizData.questions)
            }

            // Seed generic data only if subjects are empty
            if (existingSubjects.isEmpty()) {
                // 1. Seed Subjects
                val kotlinId = container.subjectRepository.insertSubject(
                    Subject(subjectName = "New Age Programming (Kotlin)", attendanceThreshold = 75.0f)
                ).toInt()
                
                val uiUxId = container.subjectRepository.insertSubject(
                    Subject(subjectName = "UI/UX Design", attendanceThreshold = 75.0f)
                ).toInt()

                // 2. Seed Tasks
                container.taskRepository.insertTask(
                    Task(
                        title = "Complete Phase II",
                        description = "Finish Room Database implementation",
                        priority = Priority.HIGH,
                        dueDate = LocalDateTime.now().plusDays(1),
                        status = TaskStatus.IN_PROGRESS
                    )
                )

                // 3. Seed Assignments
                container.assignmentRepository.insertAssignment(
                    Assignment(
                        subjectId = kotlinId,
                        title = "Room DB Implementation",
                        description = "Create entities, DAOs, and Repositories",
                        dueDate = LocalDateTime.now().plusDays(3),
                        priority = Priority.HIGH,
                        isCompleted = false
                    )
                )

                // 4. Seed Attendance Records
                container.attendanceRepository.insertRecord(
                    AttendanceRecord(
                        subjectId = kotlinId,
                        date = LocalDate.now(),
                        isPresent = true
                    )
                )
                
                container.attendanceRepository.insertRecord(
                    AttendanceRecord(
                        subjectId = uiUxId,
                        date = LocalDate.now().minusDays(1),
                        isPresent = false
                    )
                )

                // 5. Seed Notes
                container.noteRepository.insertNote(
                    Note(
                        subjectId = kotlinId,
                        title = "Coroutines and Flows",
                        content = "Flow is a cold asynchronous data stream that sequentially emits values."
                    )
                )
            }
        }
    }
}