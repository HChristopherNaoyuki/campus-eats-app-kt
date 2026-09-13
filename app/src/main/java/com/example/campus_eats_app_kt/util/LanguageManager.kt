package com.example.campus_eats_app_kt.util

import androidx.compose.runtime.mutableStateOf

/**
 * LanguageManager provides global multi-language support for the application.
 * It manages the user-selected language preference (English or Afrikaans) 
 * and handles dynamic string lookup.
 */
object LanguageManager
{
    // The current selected language state, default is English.
    var currentLanguage = mutableStateOf("English")

    /**
     * Resolves the string value based on the current selected language.
     * 
     * @param en The English version of the text.
     * @param af The Afrikaans version of the text.
     * @return The text corresponding to the current language.
     */
    fun getString(en: String, af: String): String
    {
        return if (currentLanguage.value == "English") 
        {
            en
        } 
        else 
        {
            af
        }
    }
}
