package com.example.refractiveindexapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.refractiveindexapp.settings.SharedPreferencesSettingsRepository
import com.example.refractiveindexapp.settings.ThemePreference
import com.example.refractiveindexapp.settings.ColorSchemePreference
import com.example.refractiveindexapp.settings.DatabaseVersionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsRepositoryTest {

    @Test
    fun settingsPersistAcrossRepositoryInstances() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesName = "settings-test-${System.nanoTime()}"
        val firstRepository = SharedPreferencesSettingsRepository(context, preferencesName)

        firstRepository.setUpdateCatalogueOnStartup(false)
        firstRepository.setThemePreference(ThemePreference.Dark)
        firstRepository.setColorSchemePreference(ColorSchemePreference.Ocean)
        firstRepository.setDatabaseVersionPolicy(DatabaseVersionPolicy.SpecificCommit)
        firstRepository.setDatabaseCommit("0123456")
        firstRepository.setHideUnavailableConstants(true)

        val restoredRepository = SharedPreferencesSettingsRepository(context, preferencesName)

        assertFalse(restoredRepository.settings.value.updateCatalogueOnStartup)
        assertEquals(ThemePreference.Dark, restoredRepository.settings.value.themePreference)
        assertEquals(ColorSchemePreference.Ocean, restoredRepository.settings.value.colorSchemePreference)
        assertEquals(DatabaseVersionPolicy.SpecificCommit, restoredRepository.settings.value.databaseVersionPolicy)
        assertEquals("0123456", restoredRepository.settings.value.databaseCommit)
        org.junit.Assert.assertTrue(restoredRepository.settings.value.hideUnavailableConstants)
        context.getSharedPreferences(preferencesName, 0).edit().clear().apply()
    }
}
