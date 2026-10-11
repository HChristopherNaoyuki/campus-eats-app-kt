package com.example.campus_eats_app_kt.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "campus_eats_language")

/**
 * LanguageManager provides global multi-language support for the application (English and Afrikaans).
 * Manages user language preference persistence with DataStore and handles dynamic UI string resolution.
 */
object LanguageManager
{
    private val LANGUAGE_KEY = stringPreferencesKey("selected_language")

    // The current selected language state, default is English.
    var currentLanguage = mutableStateOf("English")
        private set

    /**
     * Initializes the language manager by reading the saved language preference from DataStore.
     */
    suspend fun initialize(context: Context)
    {
        val preferences = context.dataStore.data.first()
        val savedLanguage = preferences[LANGUAGE_KEY] ?: "English"
        currentLanguage.value = savedLanguage
    }

    /**
     * Updates the current language preference in memory and persists it to DataStore.
     */
    fun setLanguage(context: Context, language: String)
    {
        currentLanguage.value = language
        CoroutineScope(Dispatchers.IO).launch()
        {
            context.dataStore.edit()
            { preferences ->
                preferences[LANGUAGE_KEY] = language
            }
        }
    }

    /**
     * Resolves the localized string value based on currentLanguage state.
     * Restricts resolution to English and Afrikaans.
     *
     * @param en The English version of the text.
     * @param af The Afrikaans version of the text.
     * @param zu Optional third string kept for backward compatibility (ignored).
     * @return The localized text corresponding to currentLanguage.value.
     */
    fun getString(en: String, af: String, zu: String = en): String
    {
        return if (currentLanguage.value == "Afrikaans")
        {
            af
        }
        else
        {
            en
        }
    }
}
