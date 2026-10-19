package com.mangareader.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mangareader.app.data.model.AppSettings
import com.mangareader.app.data.model.FitMode
import com.mangareader.app.data.model.OrientationPref
import com.mangareader.app.data.model.PageNavigation
import com.mangareader.app.data.model.ReadingDirection
import com.mangareader.app.data.model.ReadingMode
import com.mangareader.app.data.model.SortOrder
import com.mangareader.app.data.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private object Keys {
    val THEME = stringPreferencesKey("theme_mode")
    val DIRECTION = stringPreferencesKey("reading_direction")
    val MODE = stringPreferencesKey("reading_mode")
    val FIT = stringPreferencesKey("fit_mode")
    val NAVIGATION = stringPreferencesKey("page_navigation")
    val ORIENTATION = stringPreferencesKey("orientation")
    val SORT = stringPreferencesKey("sort_order")
    val ONBOARDING = booleanPreferencesKey("onboarding_done")
    val NIGHT = booleanPreferencesKey("reader_night_mode")
    val AUTO_SCAN = booleanPreferencesKey("auto_scan_on_start")
    val FOLDERS = stringSetPreferencesKey("library_folders")
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it.toSettings() }

    suspend fun setThemeMode(value: ThemeMode) = put(Keys.THEME, value)
    suspend fun setReadingDirection(value: ReadingDirection) = put(Keys.DIRECTION, value)
    suspend fun setReadingMode(value: ReadingMode) = put(Keys.MODE, value)
    suspend fun setFitMode(value: FitMode) = put(Keys.FIT, value)
    suspend fun setPageNavigation(value: PageNavigation) = put(Keys.NAVIGATION, value)
    suspend fun setOrientation(value: OrientationPref) = put(Keys.ORIENTATION, value)

    suspend fun setSortOrder(value: SortOrder) = put(Keys.SORT, value)

    suspend fun setReaderNightMode(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.NIGHT] = value }
    }

    suspend fun setOnboardingDone(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.ONBOARDING] = value }
    }

    suspend fun setAutoScanOnStart(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.AUTO_SCAN] = value }
    }

    suspend fun addLibraryFolder(uri: String) {
        context.settingsDataStore.edit { it[Keys.FOLDERS] = (it[Keys.FOLDERS] ?: emptySet()) + uri }
    }

    suspend fun removeLibraryFolder(uri: String) {
        context.settingsDataStore.edit { it[Keys.FOLDERS] = (it[Keys.FOLDERS] ?: emptySet()) - uri }
    }

    private suspend fun put(key: Preferences.Key<String>, value: Enum<*>) {
        context.settingsDataStore.edit { it[key] = value.name }
    }
}

private fun Preferences.toSettings() = AppSettings(
    themeMode = enumOrDefault(this[Keys.THEME], ThemeMode.SYSTEM),
    readingDirection = enumOrDefault(this[Keys.DIRECTION], ReadingDirection.LTR),
    readingMode = enumOrDefault(this[Keys.MODE], ReadingMode.PAGED),
    fitMode = enumOrDefault(this[Keys.FIT], FitMode.WIDTH),
    pageNavigation = enumOrDefault(this[Keys.NAVIGATION], PageNavigation.GESTURES_AND_TAP),
    orientation = enumOrDefault(this[Keys.ORIENTATION], OrientationPref.AUTO),
    sortOrder = enumOrDefault(this[Keys.SORT], SortOrder.NAME),
    readerNightMode = this[Keys.NIGHT] ?: false,
    onboardingDone = this[Keys.ONBOARDING] ?: false,
    autoScanOnStart = this[Keys.AUTO_SCAN] ?: true,
    libraryFolders = this[Keys.FOLDERS] ?: emptySet(),
)

private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == name } ?: default
