package com.example.campus_eats_app_kt.util

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
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

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "campus_eats_theme")

/**
 * ThemeManager provides in-app appearance mode management (Light, Dark, System).
 * Persists preference using DataStore and allows dynamic theme updates.
 */
object ThemeManager
{
    const val MODE_SYSTEM = "System"
    const val MODE_LIGHT = "Light"
    const val MODE_DARK = "Dark"

    private val THEME_KEY = stringPreferencesKey("appearance_mode")

    // Current selected theme mode state, default is System.
    var currentThemeMode = mutableStateOf(MODE_SYSTEM)
        private set

    /**
     * Initializes the theme manager by reading saved appearance preference from DataStore.
     */
    suspend fun initialize(context: Context)
    {
        val preferences = context.themeDataStore.data.first()
        val savedMode = preferences[THEME_KEY] ?: MODE_SYSTEM
        currentThemeMode.value = normalize(savedMode)
    }

    /**
     * Updates current theme mode in memory and persists to DataStore.
     */
    fun setThemeMode(context: Context, mode: String)
    {
        val normalizedMode = normalize(mode)
        currentThemeMode.value = normalizedMode
        CoroutineScope(Dispatchers.IO).launch()
        {
            context.themeDataStore.edit()
            { preferences ->
                preferences[THEME_KEY] = normalizedMode
            }
        }
    }

    /**
     * Determines whether dark theme should be active based on user preference and system state.
     */
    @Composable
    fun shouldFollowDarkTheme(): Boolean
    {
        return when (currentThemeMode.value)
        {
            MODE_LIGHT -> false
            MODE_DARK -> true
            else -> isSystemInDarkTheme()
        }
    }

    /**
     * Normalizes input mode string to ensure only valid options are stored.
     */
    private fun normalize(mode: String): String
    {
        return when
        {
            mode.equals(MODE_LIGHT, ignoreCase = true) -> MODE_LIGHT
            mode.equals(MODE_DARK, ignoreCase = true) -> MODE_DARK
            else -> MODE_SYSTEM
        }
    }
}
