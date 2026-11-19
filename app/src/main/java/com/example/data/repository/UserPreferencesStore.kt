package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.SupportedLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "midadd_prefs")

class UserPreferencesStore(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("preferred_language")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val languageFlow: Flow<SupportedLanguage> = context.dataStore.data.map { prefs ->
        SupportedLanguage.fromCode(prefs[Keys.LANGUAGE])
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: false
    }

    suspend fun setLanguage(language: SupportedLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setDarkMode(dark: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = dark }
    }
}
