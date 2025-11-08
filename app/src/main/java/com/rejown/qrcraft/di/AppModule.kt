package com.rejown.qrcraft.di

import com.rejown.qrcraft.data.local.preferences.ThemePreferences
import com.rejown.qrcraft.data.preferences.PreferencesManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Theme Preferences
    single { ThemePreferences(androidContext()) }

    // Preferences Manager
    single { PreferencesManager(androidContext()) }
}
