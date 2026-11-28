package com.example.campusflow.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.campusflow.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "Student",
    val university: String = "K.R. Mangalam University",
    val course: String = "B.Tech",
    val semester: String = "Semester V",
    val photoUrl: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val dailyStudyGoalMinutes: Int = 300
) {
    val isDarkMode: Boolean
        get() = themeMode == ThemeMode.DARK
}

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private object Keys {
        val UID = stringPreferencesKey("uid")
        val EMAIL = stringPreferencesKey("email")
        val NAME = stringPreferencesKey("name")
        val UNI = stringPreferencesKey("university")
        val COURSE = stringPreferencesKey("course")
        val SEMESTER = stringPreferencesKey("semester")
        val PHOTO_URL = stringPreferencesKey("photo_url")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DAILY_STUDY_GOAL_MINUTES = intPreferencesKey("daily_study_goal_minutes")
    }

    val userProfileFlow: Flow<UserProfile> = dataStore.data.map { prefs ->
        val rawTheme = prefs[Keys.THEME_MODE]
        val parsedTheme = rawTheme?.let { name ->
            try {
                ThemeMode.valueOf(name)
            } catch (_: IllegalArgumentException) {
                null
            }
        } ?: ThemeMode.SYSTEM

        UserProfile(
            uid = prefs[Keys.UID] ?: "",
            email = prefs[Keys.EMAIL] ?: "",
            name = prefs[Keys.NAME] ?: "Student",
            university = prefs[Keys.UNI] ?: "K.R. Mangalam University",
            course = prefs[Keys.COURSE] ?: "B.Tech",
            semester = prefs[Keys.SEMESTER] ?: "Semester V",
            photoUrl = prefs[Keys.PHOTO_URL] ?: "",
            themeMode = parsedTheme,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            dailyStudyGoalMinutes = prefs[Keys.DAILY_STUDY_GOAL_MINUTES] ?: 300
        )
    }

    suspend fun updateProfile(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[Keys.UID] = profile.uid
            prefs[Keys.EMAIL] = profile.email
            prefs[Keys.NAME] = profile.name
            prefs[Keys.UNI] = profile.university
            prefs[Keys.COURSE] = profile.course
            prefs[Keys.SEMESTER] = profile.semester
            prefs[Keys.PHOTO_URL] = profile.photoUrl
            prefs[Keys.THEME_MODE] = profile.themeMode.name
            prefs[Keys.NOTIFICATIONS_ENABLED] = profile.notificationsEnabled
            prefs[Keys.DAILY_STUDY_GOAL_MINUTES] = profile.dailyStudyGoalMinutes
        }
    }

    suspend fun clearUserProfile() {
        dataStore.edit { prefs ->
            val currentTheme = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            prefs.clear()
            prefs[Keys.THEME_MODE] = currentTheme
        }
    }
}
