package com.rejown.qrcraft.data.repository

import com.rejown.qrcraft.data.local.preferences.ThemePreferences
import com.rejown.qrcraft.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val themePreferences: ThemePreferences
) : SettingsRepository {

    override fun getTheme(): Flow<String> {
        return themePreferences.getTheme()
    }

    override suspend fun setTheme(theme: String) {
        themePreferences.setTheme(theme)
    }

    override fun isDynamicColorEnabled(): Flow<Boolean> {
        return themePreferences.isDynamicColorEnabled()
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        themePreferences.setDynamicColor(enabled)
    }
}
