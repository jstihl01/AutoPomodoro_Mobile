package com.jstihl01.autopomodoro.mobile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class AppSettings(val mode: DisplayMode = DisplayMode.STANDARD, val volumePercent: Int = 60)

class SettingsRepository(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.settingsDataStore)

    val settings = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { preferences ->
        val mode = DisplayMode.entries.firstOrNull { it.name == preferences[MODE] } ?: DisplayMode.STANDARD
        AppSettings(mode, (preferences[VOLUME] ?: 60).coerceIn(0, 100))
    }

    suspend fun setMode(mode: DisplayMode) { store.edit { it[MODE] = mode.name } }
    suspend fun setVolume(percent: Int) { store.edit { it[VOLUME] = percent.coerceIn(0, 100) } }
    suspend fun reset() { store.edit { it.clear() } }

    private companion object {
        val MODE = stringPreferencesKey("mode")
        val VOLUME = intPreferencesKey("volume_percent")
    }
}
