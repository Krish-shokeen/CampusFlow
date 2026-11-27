package com.example.campusflow

import com.example.campusflow.utils.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import kotlin.math.ceil

class CalculationsTest {

    @Test
    fun testDateUtilsOverdueCalculation() {
        val pastDate = LocalDateTime.now().minusDays(1)
        val futureDate = LocalDateTime.now().plusDays(1)

        assertTrue("Past date should be overdue", DateUtils.isOverdue(pastDate))
        assertFalse("Future date should not be overdue", DateUtils.isOverdue(futureDate))
        assertFalse("Null date should not be overdue", DateUtils.isOverdue(null))
    }

    @Test
    fun testDateUtilsDaysRemaining() {
        val futureDate = LocalDateTime.now().plusDays(5)
        val daysRemaining = DateUtils.getDaysRemaining(futureDate)

        assertEquals("Should have 5 days remaining", 5L, daysRemaining)
    }

    @Test
    fun testAttendancePercentageCalculation() {
        val present = 42
        val total = 48
        val percentage = (present.toFloat() / total) * 100f

        assertEquals("42/48 should be 87.5%", 87.5f, percentage, 0.01f)
    }

    @Test
    fun testAttendanceRequiredClassesCalculation() {
        val present = 20
        val total = 30
        val thresholdRatio = 0.75f // 75%
        
        // (0.75 * 30 - 20) / (1 - 0.75)
        // (22.5 - 20) / 0.25 = 2.5 / 0.25 = 10
        val needed = ceil((thresholdRatio * total - present) / (1 - thresholdRatio)).toInt()

        assertEquals("Should need 10 more consecutive classes to reach 75%", 10, needed)
    }
    
    @Test
    fun testQuizScoreCalculation() {
        val userAnswers = mapOf(0 to "A", 1 to "B", 2 to "C")
        val correctAnswers = listOf("A", "B", "D") // Question 3 is wrong

        var score = 0
        correctAnswers.forEachIndexed { index, correct ->
            if (userAnswers[index] == correct) {
                score++
            }
        }

        assertEquals("Score should be 2 out of 3", 2, score)
        
        val percentage = (score.toFloat() / correctAnswers.size) * 100f
        assertEquals("Percentage should be 66.66%", 66.66f, percentage, 0.01f)
    }
}