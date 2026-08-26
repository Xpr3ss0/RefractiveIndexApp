package com.example.refractiveindexapp.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemePreference {
    System,
    Light,
    Dark
}

enum class ColorSchemePreference { System, Purple, Ocean, Forest }
enum class DatabaseVersionPolicy { Latest, SpecificCommit }

data class AppSettings(
    val updateCatalogueOnStartup: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.System,
    val colorSchemePreference: ColorSchemePreference = ColorSchemePreference.System,
    val databaseVersionPolicy: DatabaseVersionPolicy = DatabaseVersionPolicy.Latest,
    val databaseCommit: String = "",
    val hideUnavailableConstants: Boolean = false
)

interface SettingsRepository {
    val settings: StateFlow<AppSettings>

    fun setUpdateCatalogueOnStartup(enabled: Boolean)
    fun setThemePreference(preference: ThemePreference)
    fun setColorSchemePreference(preference: ColorSchemePreference)
    fun setDatabaseVersionPolicy(policy: DatabaseVersionPolicy)
    fun setDatabaseCommit(commit: String)
    fun setHideUnavailableConstants(hide: Boolean)
}

class SharedPreferencesSettingsRepository(
    context: Context,
    preferencesName: String = DEFAULT_PREFERENCES_NAME
) : SettingsRepository {
    private val preferences = context.applicationContext.getSharedPreferences(
        preferencesName,
        Context.MODE_PRIVATE
    )
    private val mutableSettings = MutableStateFlow(readSettings())

    override val settings: StateFlow<AppSettings> = mutableSettings.asStateFlow()

    override fun setUpdateCatalogueOnStartup(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_UPDATE_CATALOGUE_ON_STARTUP, enabled).apply()
        mutableSettings.value = mutableSettings.value.copy(updateCatalogueOnStartup = enabled)
    }

    override fun setThemePreference(preference: ThemePreference) {
        preferences.edit().putString(KEY_THEME_PREFERENCE, preference.name).apply()
        mutableSettings.value = mutableSettings.value.copy(themePreference = preference)
    }
    override fun setColorSchemePreference(preference: ColorSchemePreference) { preferences.edit().putString(KEY_COLOR_SCHEME, preference.name).apply(); update { it.copy(colorSchemePreference = preference) } }
    override fun setDatabaseVersionPolicy(policy: DatabaseVersionPolicy) { preferences.edit().putString(KEY_DATABASE_POLICY, policy.name).apply(); update { it.copy(databaseVersionPolicy = policy) } }
    override fun setDatabaseCommit(commit: String) { preferences.edit().putString(KEY_DATABASE_COMMIT, commit).apply(); update { it.copy(databaseCommit = commit) } }
    override fun setHideUnavailableConstants(hide: Boolean) { preferences.edit().putBoolean(KEY_HIDE_UNAVAILABLE, hide).apply(); update { it.copy(hideUnavailableConstants = hide) } }
    private fun update(transform: (AppSettings) -> AppSettings) { mutableSettings.value = transform(mutableSettings.value) }

    private fun readSettings(): AppSettings = AppSettings(
        updateCatalogueOnStartup = preferences.getBoolean(KEY_UPDATE_CATALOGUE_ON_STARTUP, true),
        themePreference = preferences.getString(KEY_THEME_PREFERENCE, ThemePreference.System.name)
            ?.let { storedValue -> ThemePreference.entries.firstOrNull { it.name == storedValue } }
            ?: ThemePreference.System,
        colorSchemePreference = preferences.getString(KEY_COLOR_SCHEME, ColorSchemePreference.System.name)?.let { value -> ColorSchemePreference.entries.firstOrNull { it.name == value } } ?: ColorSchemePreference.System,
        databaseVersionPolicy = preferences.getString(KEY_DATABASE_POLICY, DatabaseVersionPolicy.Latest.name)?.let { value -> DatabaseVersionPolicy.entries.firstOrNull { it.name == value } } ?: DatabaseVersionPolicy.Latest,
        databaseCommit = preferences.getString(KEY_DATABASE_COMMIT, "").orEmpty(),
        hideUnavailableConstants = preferences.getBoolean(KEY_HIDE_UNAVAILABLE, false)
    )

    private companion object {
        const val DEFAULT_PREFERENCES_NAME = "app_settings"
        const val KEY_UPDATE_CATALOGUE_ON_STARTUP = "update_catalogue_on_startup"
        const val KEY_THEME_PREFERENCE = "theme_preference"
        const val KEY_COLOR_SCHEME = "color_scheme"
        const val KEY_DATABASE_POLICY = "database_policy"
        const val KEY_DATABASE_COMMIT = "database_commit"
        const val KEY_HIDE_UNAVAILABLE = "hide_unavailable_constants"
    }
}

class InMemorySettingsRepository(
    initialSettings: AppSettings = AppSettings()
) : SettingsRepository {
    private val mutableSettings = MutableStateFlow(initialSettings)
    override val settings: StateFlow<AppSettings> = mutableSettings.asStateFlow()

    override fun setUpdateCatalogueOnStartup(enabled: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(updateCatalogueOnStartup = enabled)
    }

    override fun setThemePreference(preference: ThemePreference) {
        mutableSettings.value = mutableSettings.value.copy(themePreference = preference)
    }
    override fun setColorSchemePreference(preference: ColorSchemePreference) { mutableSettings.value = mutableSettings.value.copy(colorSchemePreference = preference) }
    override fun setDatabaseVersionPolicy(policy: DatabaseVersionPolicy) { mutableSettings.value = mutableSettings.value.copy(databaseVersionPolicy = policy) }
    override fun setDatabaseCommit(commit: String) { mutableSettings.value = mutableSettings.value.copy(databaseCommit = commit) }
    override fun setHideUnavailableConstants(hide: Boolean) { mutableSettings.value = mutableSettings.value.copy(hideUnavailableConstants = hide) }
}
