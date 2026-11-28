package com.example.campusflow

import com.example.campusflow.data.entities.Assignment
import com.example.campusflow.data.entities.AttendanceRecord
import com.example.campusflow.data.entities.Priority
import com.example.campusflow.data.entities.QuizResult
import com.example.campusflow.data.entities.StudySession
import com.example.campusflow.data.entities.Task
import com.example.campusflow.data.entities.TaskStatus
import com.example.campusflow.data.remote.FirestoreMapper
import com.example.campusflow.data.repository.AttendanceRepository
import com.example.campusflow.data.repository.StatisticsRepository
import com.example.campusflow.data.repository.StudyPlannerRepository
import com.example.campusflow.data.seeder.QuizData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class BackendDomainLogicTest {

    @Test
    fun attendanceCalculator_computesPercentageAndClassesNeeded() {
        val pct = AttendanceRepository.calculatePercentage(present = 15, total = 20)
        assertEquals(75.0f, pct, 0.01f)
        assertEquals(0, AttendanceRepository.calculateClassesToReachThreshold(15, 20, 75f))

        // 20/30 = 66.67% -> needs 10 consecutive classes to reach 75% (30/40)
        assertEquals(10, AttendanceRepository.calculateClassesToReachThreshold(20, 30, 75f))
    }

    @Test(expected = IllegalArgumentException::class)
    fun studySessionValidation_rejectsZeroOrNegativeDuration() {
        StudyPlannerRepository.validateSessionTime(LocalDateTime.now(), 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun studySessionValidation_rejectsEndTimeBeforeStartTime() {
        val start = LocalDateTime.of(2026, 11, 27, 15, 0)
        val end = LocalDateTime.of(2026, 11, 27, 14, 0)
        StudyPlannerRepository.calculateDurationMinutes(start, end)
    }

    @Test
    fun studySessionValidation_calculatesDurationCorrectly() {
        val start = LocalDateTime.of(2026, 11, 27, 14, 0)
        val end = LocalDateTime.of(2026, 11, 27, 15, 30)
        assertEquals(90, StudyPlannerRepository.calculateDurationMinutes(start, end))
    }

    @Test
    fun statisticsRepository_aggregatesRealRecordsWithoutFakeValues() {
        val today = LocalDate.of(2026, 11, 27)
        val tasks = listOf(
            Task(1, "Task 1", "", Priority.HIGH, null, TaskStatus.COMPLETED),
            Task(2, "Task 2", "", Priority.LOW, null, TaskStatus.PENDING)
        )
        val assignments = listOf(
            Assignment(1, 1, "A1", "", LocalDateTime.now().minusDays(1), Priority.HIGH, isCompleted = false),
            Assignment(2, 1, "A2", "", LocalDateTime.now().plusDays(2), Priority.MEDIUM, isCompleted = true)
        )
        val attendance = listOf(
            AttendanceRecord(1, 1, today, isPresent = true),
            AttendanceRecord(2, 1, today.minusDays(1), isPresent = true),
            AttendanceRecord(3, 1, today.minusDays(2), isPresent = false),
            AttendanceRecord(4, 1, today.minusDays(3), isPresent = true)
        )
        val quizzes = listOf(
            QuizResult(1, "Kotlin", score = 8, totalQuestions = 10, timeTakenSeconds = 120),
            QuizResult(2, "DBMS", score = 10, totalQuestions = 10, timeTakenSeconds = 95)
        )
        val sessions = listOf(
            StudySession(1, 1, today, LocalDateTime.of(2026, 11, 27, 10, 0), 90),
            StudySession(2, 1, today.minusDays(10), LocalDateTime.of(2026, 11, 17, 10, 0), 60)
        )

        val stats = StatisticsRepository.computeStatistics(
            tasks = tasks,
            assignments = assignments,
            attendance = attendance,
            quizzes = quizzes,
            sessions = sessions,
            today = today
        )

        assertEquals(2, stats.totalTasks)
        assertEquals(1, stats.completedTasks)
        assertEquals(1, stats.pendingTasks)
        assertEquals(2, stats.totalAssignments)
        assertEquals(1, stats.completedAssignments)
        assertEquals(1, stats.overdueAssignments)
        assertEquals(50.0f, stats.assignmentCompletionPercentage, 0.01f)
        assertEquals(3, stats.attendedClasses)
        assertEquals(1, stats.missedClasses)
        assertEquals(4, stats.totalClasses)
        assertEquals(75.0f, stats.attendancePercentage, 0.01f)
        assertEquals(2, stats.totalQuizzesAttempted)
        assertEquals(90.0f, stats.averageQuizScore, 0.01f)
        assertEquals(100.0f, stats.bestQuizScore, 0.01f)
        assertEquals(150, stats.totalStudyMinutes)
        assertEquals(2.5f, stats.totalStudyHours, 0.01f)
        assertEquals(90, stats.weeklyStudyMinutes)
        assertTrue(stats.weeklyProductivityScore in 1..100)
    }

    @Test
    fun quizData_containsAllSixRequiredCategories() {
        val categories = QuizData.questions.map { it.category }.toSet()
        assertTrue(categories.contains("Kotlin"))
        assertTrue(categories.contains("Java"))
        assertTrue(categories.contains("DBMS"))
        assertTrue(categories.contains("Data Structures and Algorithms"))
        assertTrue(categories.contains("Operating Systems"))
        assertTrue(categories.contains("General Computer Science"))
    }

    @Test
    fun firestoreMapper_producesDeterministicPositiveNonZeroIds() {
        val id1 = FirestoreMapper.stableIntId("doc_abc_123")
        val id2 = FirestoreMapper.stableIntId("doc_abc_123")
        val id3 = FirestoreMapper.stableIntId("doc_xyz_789")
        assertEquals(id1, id2)
        assertTrue(id1 > 0)
        assertNotEquals(id1, id3)
    }
}
