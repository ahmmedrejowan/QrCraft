package com.rejown.qrcraft.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getTheme(): Flow<String>
    suspend fun setTheme(theme: String)
    fun isDynamicColorEnabled(): Flow<Boolean>
    suspend fun setDynamicColor(enabled: Boolean)
}
