package com.example.campusflow.data.remote

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object FirestoreMapper {

    fun stableIntId(docId: String): Int {
        val positiveHash = docId.hashCode() and 0x7FFFFFFF
        return if (positiveHash == 0) 1 else positiveHash
    }

    fun localDateTimeToMillis(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun millisToLocalDateTime(millis: Long): LocalDateTime {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    fun localDateToIso(date: LocalDate): String = date.toString()

    fun isoToLocalDate(iso: String?): LocalDate {
        return try {
            if (iso.isNullOrBlank()) LocalDate.now() else LocalDate.parse(iso)
        } catch (_: Exception) {
            LocalDate.now()
        }
    }
}
