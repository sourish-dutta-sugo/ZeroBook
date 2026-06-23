package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "zerobook_prefs")

object AppPreferences {
    private val fyLastCheckedDateKey = stringPreferencesKey("fy_last_checked_date")
    private val lastSeenChangelogVersionKey = stringPreferencesKey("last_seen_changelog_version")

    suspend fun getFyLastCheckedDate(context: Context): LocalDate? =
        context.dataStore.data.first()[fyLastCheckedDateKey]?.let(LocalDate::parse)

    suspend fun setFyLastCheckedDate(context: Context, date: LocalDate) {
        context.dataStore.edit { prefs ->
            prefs[fyLastCheckedDateKey] = date.toString()
        }
    }

    suspend fun getLastSeenChangelogVersion(context: Context): String? =
        context.dataStore.data.first()[lastSeenChangelogVersionKey]

    suspend fun setLastSeenChangelogVersion(context: Context, version: String) {
        context.dataStore.edit { prefs ->
            prefs[lastSeenChangelogVersionKey] = version
        }
    }
}
