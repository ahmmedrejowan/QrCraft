package com.rejown.qrcraft.di

import com.rejown.qrcraft.data.local.preferences.ThemePreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Theme Preferences
    single { ThemePreferences(androidContext()) }
}
