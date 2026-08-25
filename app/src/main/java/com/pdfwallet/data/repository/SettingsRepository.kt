package com.pdfwallet.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.pdfwallet.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_IMAGE = stringPreferencesKey("user_image")
        val LAST_BACKUP_TIME = androidx.datastore.preferences.core.longPreferencesKey("last_backup_time")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val useDynamicColorFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLOR] ?: true // default true
    }

    suspend fun setUseDynamicColor(useDynamic: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLOR] = useDynamic
        }
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED] = enabled
        }
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME] ?: "Wallet User"
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    val userEmailFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }

    suspend fun setUserEmail(email: String?) {
        context.dataStore.edit { preferences ->
            if (email == null) {
                preferences.remove(USER_EMAIL)
            } else {
                preferences[USER_EMAIL] = email
            }
        }
    }

    val userImageFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_IMAGE]
    }

    suspend fun setUserImage(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(USER_IMAGE)
            } else {
                preferences[USER_IMAGE] = uri
            }
        }
    }

    val lastBackupTimeFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_BACKUP_TIME] ?: 0L
    }

    suspend fun setLastBackupTime(timeMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_BACKUP_TIME] = timeMillis
        }
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_MODE]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    private val AI_EXTRACTION_KEY = booleanPreferencesKey("ai_extraction_enabled")

    val aiExtractionEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[AI_EXTRACTION_KEY] ?: true }

    suspend fun setAiExtractionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AI_EXTRACTION_KEY] = enabled
        }
    }
}
