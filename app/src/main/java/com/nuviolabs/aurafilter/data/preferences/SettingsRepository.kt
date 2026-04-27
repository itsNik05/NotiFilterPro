package com.nuviolabs.aurafilter.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_settings")

data class WeeklyTrendPoint(
    val dayLabel: String,
    val blocked: Int,
    val review: Int,
    val allowed: Int
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DIGEST_INTERVAL_KEY = intPreferencesKey("digest_interval_hours")
    private val BLOCKED_COUNT_KEY = intPreferencesKey("blocked_count")
    private val ALLOWED_COUNT_KEY = intPreferencesKey("allowed_count")
    private val ACCENT_COLOR_KEY = intPreferencesKey("accent_color")
    private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
    private val HAS_SEEN_ONBOARDING_KEY = booleanPreferencesKey("has_seen_onboarding")
    private val WEEKLY_ALLOWED_KEY = stringPreferencesKey("weekly_allowed_counts")
    private val WEEKLY_BLOCKED_KEY = stringPreferencesKey("weekly_blocked_counts")
    private val WEEKLY_REVIEW_KEY = stringPreferencesKey("weekly_review_counts")

    val digestIntervalFlow: Flow<Int> = context.dataStore.data.map { it[DIGEST_INTERVAL_KEY] ?: 2 }
    val blockedCountFlow: Flow<Int> = context.dataStore.data.map { it[BLOCKED_COUNT_KEY] ?: 0 }
    val allowedCountFlow: Flow<Int> = context.dataStore.data.map { it[ALLOWED_COUNT_KEY] ?: 0 }
    val accentColorFlow: Flow<Int> = context.dataStore.data.map { it[ACCENT_COLOR_KEY] ?: 0 }
    val weeklyAllowedCountFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        sumLast7Days(parseDailyCounts(prefs[WEEKLY_ALLOWED_KEY]))
    }
    val weeklyBlockedCountFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        sumLast7Days(parseDailyCounts(prefs[WEEKLY_BLOCKED_KEY]))
    }
    val weeklyReviewCountFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        sumLast7Days(parseDailyCounts(prefs[WEEKLY_REVIEW_KEY]))
    }
    val weeklyTrendFlow: Flow<List<WeeklyTrendPoint>> = context.dataStore.data.map { prefs ->
        val blocked = pruneToLast7Days(parseDailyCounts(prefs[WEEKLY_BLOCKED_KEY]))
        val review = pruneToLast7Days(parseDailyCounts(prefs[WEEKLY_REVIEW_KEY]))
        val allowed = pruneToLast7Days(parseDailyCounts(prefs[WEEKLY_ALLOWED_KEY]))
        last7Days().map { date ->
            WeeklyTrendPoint(
                dayLabel = dayLabel(date.dayOfWeek),
                blocked = blocked[date] ?: 0,
                review = review[date] ?: 0,
                allowed = allowed[date] ?: 0
            )
        }
    }
    val hasSeenOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_SEEN_ONBOARDING_KEY] ?: false
    }

    val isDarkModeFlow: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE_KEY]
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDark
        }
    }

    suspend fun useSystemTheme() {
        context.dataStore.edit { preferences ->
            preferences.remove(IS_DARK_MODE_KEY)
        }
    }

    suspend fun markOnboardingSeen() {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING_KEY] = true
        }
    }

    suspend fun saveDigestInterval(hours: Int) {
        context.dataStore.edit { it[DIGEST_INTERVAL_KEY] = hours }
    }

    suspend fun saveAccentColor(accentId: Int) {
        context.dataStore.edit { it[ACCENT_COLOR_KEY] = accentId }
    }

    suspend fun incrementBlockedCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[BLOCKED_COUNT_KEY] ?: 0
            preferences[BLOCKED_COUNT_KEY] = current + 1
            preferences[WEEKLY_BLOCKED_KEY] = serializeDailyCounts(
                incrementDailyCount(parseDailyCounts(preferences[WEEKLY_BLOCKED_KEY]))
            )
        }
    }

    suspend fun incrementAllowedCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[ALLOWED_COUNT_KEY] ?: 0
            preferences[ALLOWED_COUNT_KEY] = current + 1
            preferences[WEEKLY_ALLOWED_KEY] = serializeDailyCounts(
                incrementDailyCount(parseDailyCounts(preferences[WEEKLY_ALLOWED_KEY]))
            )
        }
    }

    suspend fun incrementReviewCount() {
        context.dataStore.edit { preferences ->
            preferences[WEEKLY_REVIEW_KEY] = serializeDailyCounts(
                incrementDailyCount(parseDailyCounts(preferences[WEEKLY_REVIEW_KEY]))
            )
        }
    }

    private fun incrementDailyCount(existing: Map<LocalDate, Int>): Map<LocalDate, Int> {
        val today = LocalDate.now()
        val pruned = pruneToLast7Days(existing).toMutableMap()
        pruned[today] = (pruned[today] ?: 0) + 1
        return pruned
    }

    private fun pruneToLast7Days(source: Map<LocalDate, Int>): Map<LocalDate, Int> {
        val cutoff = LocalDate.now().minusDays(6)
        return source.filterKeys { !it.isBefore(cutoff) }
    }

    private fun last7Days(): List<LocalDate> {
        val today = LocalDate.now()
        return (6 downTo 0).map { today.minusDays(it.toLong()) }
    }

    private fun sumLast7Days(source: Map<LocalDate, Int>): Int {
        return pruneToLast7Days(source).values.sum()
    }

    private fun parseDailyCounts(raw: String?): Map<LocalDate, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split("|")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size != 2) return@mapNotNull null
                val date = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: return@mapNotNull null
                val count = parts[1].toIntOrNull() ?: return@mapNotNull null
                date to count
            }
            .toMap()
    }

    private fun serializeDailyCounts(source: Map<LocalDate, Int>): String {
        return pruneToLast7Days(source)
            .toSortedMap()
            .entries
            .joinToString("|") { (date, count) -> "$date:$count" }
    }

    private fun dayLabel(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "M"
        DayOfWeek.TUESDAY -> "T"
        DayOfWeek.WEDNESDAY -> "W"
        DayOfWeek.THURSDAY -> "T"
        DayOfWeek.FRIDAY -> "F"
        DayOfWeek.SATURDAY -> "S"
        DayOfWeek.SUNDAY -> "S"
    }
}
