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
    const val ENGLISH = "English"
    const val AFRIKAANS = "Afrikaans"

    private val LANGUAGE_KEY = stringPreferencesKey("selected_language")

    // The current selected language state, default is English.
    var currentLanguage = mutableStateOf(ENGLISH)
        private set

    /**
     * Normalizes the language string to ensure only English or Afrikaans is applied.
     */
    fun normalize(language: String): String
    {
        return if (language.equals(AFRIKAANS, ignoreCase = true))
        {
            AFRIKAANS
        }
        else
        {
            ENGLISH
        }
    }

    /**
     * Initializes the language manager by reading the saved language preference from DataStore.
     */
    suspend fun initialize(context: Context)
    {
        val preferences = context.dataStore.data.first()
        val savedLanguage = preferences[LANGUAGE_KEY] ?: ENGLISH
        currentLanguage.value = normalize(savedLanguage)
    }

    /**
     * Updates the current language preference in memory and persists it to DataStore.
     */
    fun setLanguage(context: Context, language: String)
    {
        val normalizedLanguage = normalize(language)
        currentLanguage.value = normalizedLanguage
        CoroutineScope(Dispatchers.IO).launch()
        {
            context.dataStore.edit()
            { preferences ->
                preferences[LANGUAGE_KEY] = normalizedLanguage
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
    fun getString(en: String, af: String, @Suppress("UNUSED_PARAMETER") zu: String = en): String
    {
        return if (currentLanguage.value == AFRIKAANS)
        {
            af
        }
        else
        {
            en
        }
    }
}
