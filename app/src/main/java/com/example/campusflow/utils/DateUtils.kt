package com.example.campusflow.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    private val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    fun formatDateTime(dateTime: LocalDateTime?): String {
        return dateTime?.format(formatter) ?: "Not set"
    }

    fun formatDate(dateTime: LocalDateTime?): String {
        return dateTime?.format(dateFormatter) ?: "Not set"
    }

    fun formatLocalDate(date: LocalDate?): String {
        return date?.format(dateFormatter) ?: "Not set"
    }

    fun convertMillisToLocalDateTime(millis: Long): LocalDateTime {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    fun convertLocalDateTimeToMillis(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun convertMillisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun convertLocalDateToMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    fun isOverdue(dueDate: LocalDateTime?): Boolean {
        if (dueDate == null) return false
        return dueDate.isBefore(LocalDateTime.now())
    }

    fun getDaysRemaining(dueDate: LocalDateTime?): Long? {
        if (dueDate == null) return null
        return ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), dueDate.toLocalDate())
    }
}