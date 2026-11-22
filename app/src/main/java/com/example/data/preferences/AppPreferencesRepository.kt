package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dailyfix_prefs")

class AppPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PRIVACY_POLICY_URL = stringPreferencesKey("privacy_policy_url")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val stored = prefs[Keys.THEME_MODE]
        ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM
    }

    val privacyPolicyUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIVACY_POLICY_URL]?.takeIf { it.isNotBlank() } ?: BuildConfig.PRIVACY_POLICY_URL
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setPrivacyPolicyUrl(url: String) {
        context.dataStore.edit { prefs ->
            val trimmed = url.trim()
            if (trimmed.isEmpty()) {
                prefs.remove(Keys.PRIVACY_POLICY_URL)
            } else {
                prefs[Keys.PRIVACY_POLICY_URL] = trimmed
            }
        }
    }
}
