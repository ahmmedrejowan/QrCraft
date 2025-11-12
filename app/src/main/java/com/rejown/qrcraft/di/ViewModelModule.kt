package com.rejown.qrcraft.di

import com.rejown.qrcraft.presentation.generator.GeneratorViewModel
import com.rejown.qrcraft.presentation.generator.creation.CreationViewModel
import com.rejown.qrcraft.presentation.generator.details.CodeDetailViewModel
import com.rejown.qrcraft.presentation.history.HistoryViewModel
import com.rejown.qrcraft.presentation.scanner.ScannerViewModel
import com.rejown.qrcraft.presentation.scanner.details.ScanHistoryDetailViewModel
import com.rejown.qrcraft.presentation.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    // Scanner - single instance to preserve state across navigation
    single { ScannerViewModel(get()) }

    // Scan History Detail
    viewModel { ScanHistoryDetailViewModel(get()) }

    // Generator
    viewModel { GeneratorViewModel(get()) }

    // Creation
    viewModel { CreationViewModel(androidContext(), get()) }

    // Code Detail
    viewModel { CodeDetailViewModel(androidContext(), get()) }

    // History
    viewModel { HistoryViewModel(get(), get()) }

    // Settings
    viewModel { SettingsViewModel(get(), get(), get()) }
}
