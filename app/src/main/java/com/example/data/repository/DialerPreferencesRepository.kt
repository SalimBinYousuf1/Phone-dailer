package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dialer_preferences")

class DialerPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "system", "light", "dark"
        val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
        val KEY_DIALPAD_TONES = booleanPreferencesKey("dialpad_tones_enabled")
        val KEY_DEFAULT_SIM = intPreferencesKey("default_sim") // 0: SIM 1, 1: SIM 2, -1: Ask
        val KEY_SILENCE_UNKNOWN = booleanPreferencesKey("silence_unknown")
        val KEY_AUTO_FORMAT = booleanPreferencesKey("auto_format")
        val KEY_QUICK_RESPONSES = stringPreferencesKey("quick_responses")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "system"
    }

    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTICS] ?: true
    }

    val dialpadTonesEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DIALPAD_TONES] ?: true
    }

    val defaultSim: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_SIM] ?: 0
    }

    val silenceUnknown: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SILENCE_UNKNOWN] ?: false
    }

    val autoFormat: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_FORMAT] ?: true
    }

    val quickResponses: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_QUICK_RESPONSES]
        if (raw.isNullOrBlank()) {
            listOf(
                "Can't talk now. What's up?",
                "I'll call you right back.",
                "In a meeting. Will ping you later.",
                "On my way."
            )
        } else {
            raw.split("||")
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTICS] = enabled }
    }

    suspend fun setDialpadTonesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DIALPAD_TONES] = enabled }
    }

    suspend fun setDefaultSim(sim: Int) {
        context.dataStore.edit { it[KEY_DEFAULT_SIM] = sim }
    }

    suspend fun setSilenceUnknown(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SILENCE_UNKNOWN] = enabled }
    }

    suspend fun setAutoFormat(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_FORMAT] = enabled }
    }

    suspend fun updateQuickResponses(responses: List<String>) {
        context.dataStore.edit { it[KEY_QUICK_RESPONSES] = responses.joinToString("||") }
    }
}
