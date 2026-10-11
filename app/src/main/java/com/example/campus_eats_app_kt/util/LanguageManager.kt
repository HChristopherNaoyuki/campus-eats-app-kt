package com.example.campus_eats_app_kt.util

import androidx.compose.runtime.mutableStateOf

/**
 * LanguageManager provides global multi-language support for the application.
 * Manages the user-selected language preference (English, Afrikaans, or isiZulu)
 * and resolves localized strings dynamically across UI chrome components.
 */
object LanguageManager
{
    // The current selected language state, default is English.
    var currentLanguage = mutableStateOf("English")

    /**
     * Resolves the string value based on the current selected language state.
     *
     * @param en The English version of the text.
     * @param af The Afrikaans version of the text.
     * @param zu The isiZulu version of the text (defaults to English if omitted).
     * @return The localized text corresponding to currentLanguage.value.
     */
    fun getString(en: String, af: String, zu: String = en): String
    {
        return when (currentLanguage.value)
        {
            "Afrikaans" -> af
            "isiZulu" -> zu
            else -> en
        }
    }
}
