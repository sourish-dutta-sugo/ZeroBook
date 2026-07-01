package com.zerobook.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "zerobook_prefs")

object AppPreferences {
    private val fyLastCheckedDateKey = stringPreferencesKey("fy_last_checked_date")
    private val lastSeenChangelogVersionKey = stringPreferencesKey("last_seen_changelog_version")
    private val progressTrackerEnabledKey = booleanPreferencesKey("progress_tracker_enabled")
    private val progressTrackerMetricKey = stringPreferencesKey("progress_tracker_metric")
    private val progressTrackerPeriodKey = stringPreferencesKey("progress_tracker_period")
    private val progressTrackerTargetKey = stringPreferencesKey("progress_tracker_target")

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

    suspend fun isProgressTrackerEnabled(context: Context): Boolean =
        context.dataStore.data.first()[progressTrackerEnabledKey] ?: false

    suspend fun setProgressTrackerEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[progressTrackerEnabledKey] = enabled
        }
    }

    suspend fun getProgressTrackerMetric(context: Context): String =
        context.dataStore.data.first()[progressTrackerMetricKey] ?: "Sales"

    suspend fun setProgressTrackerMetric(context: Context, metric: String) {
        context.dataStore.edit { prefs ->
            prefs[progressTrackerMetricKey] = metric
        }
    }

    suspend fun getProgressTrackerPeriod(context: Context): String =
        context.dataStore.data.first()[progressTrackerPeriodKey] ?: "Monthly"

    suspend fun setProgressTrackerPeriod(context: Context, period: String) {
        context.dataStore.edit { prefs ->
            prefs[progressTrackerPeriodKey] = period
        }
    }

    suspend fun getProgressTrackerTarget(context: Context): String =
        context.dataStore.data.first()[progressTrackerTargetKey] ?: "200000"

    suspend fun setProgressTrackerTarget(context: Context, target: String) {
        context.dataStore.edit { prefs ->
            prefs[progressTrackerTargetKey] = target
        }
    }
}
