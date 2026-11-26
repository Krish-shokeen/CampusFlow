package com.example.campusflow.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.campusflow.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferences by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val name: String = "",
    val course: String = "",
    val semester: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val name = stringPreferencesKey("profile_name")
        val course = stringPreferencesKey("profile_course")
        val semester = stringPreferencesKey("profile_semester")
        val themeMode = stringPreferencesKey("theme_mode")
    }

    val preferences: Flow<UserPreferences> = context.userPreferences.data.map { values ->
        UserPreferences(
            name = values[Keys.name].orEmpty(),
            course = values[Keys.course].orEmpty(),
            semester = values[Keys.semester].orEmpty(),
            themeMode = values[Keys.themeMode]
                ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                ?: ThemeMode.SYSTEM
        )
    }

    suspend fun saveProfile(name: String, course: String, semester: String) {
        context.userPreferences.edit { values ->
            values[Keys.name] = name.trim()
            values[Keys.course] = course.trim()
            values[Keys.semester] = semester.trim()
        }
    }

    suspend fun saveThemeMode(themeMode: ThemeMode) {
        context.userPreferences.edit { values -> values[Keys.themeMode] = themeMode.name }
    }
}
