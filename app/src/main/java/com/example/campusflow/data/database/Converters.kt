package com.example.campusflow.data.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

// TypeConverters teach Room how to store complex data types (like LocalDateTime) 
// into primitive types (like String) that SQLite understands.
class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.toString()
    }
}