package com.example.campusflow.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserProfile(
    val name: String = "Student",
    val university: String = "K.R. Mangalam University",
    val course: String = "B.Tech",
    val semester: String = "Semester V",
    val isDarkMode: Boolean = false
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private val NAME = stringPreferencesKey("name")
    private val UNI = stringPreferencesKey("university")
    private val COURSE = stringPreferencesKey("course")
    private val SEMESTER = stringPreferencesKey("semester")
    private val DARK_MODE = booleanPreferencesKey("dark_mode")

    val userProfileFlow: Flow<UserProfile> = dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[NAME] ?: "Student",
            university = prefs[UNI] ?: "K.R. Mangalam University",
            course = prefs[COURSE] ?: "B.Tech",
            semester = prefs[SEMESTER] ?: "Semester V",
            isDarkMode = prefs[DARK_MODE] ?: false
        )
    }

    suspend fun updateProfile(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[NAME] = profile.name
            prefs[UNI] = profile.university
            prefs[COURSE] = profile.course
            prefs[SEMESTER] = profile.semester
            prefs[DARK_MODE] = profile.isDarkMode
        }
    }
}