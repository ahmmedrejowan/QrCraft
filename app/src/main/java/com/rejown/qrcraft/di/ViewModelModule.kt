package com.rejown.qrcraft.di

import com.rejown.qrcraft.presentation.generator.GeneratorViewModel
import com.rejown.qrcraft.presentation.generator.creation.CreationViewModel
import com.rejown.qrcraft.presentation.generator.details.CodeDetailViewModel
import com.rejown.qrcraft.presentation.history.HistoryViewModel
import com.rejown.qrcraft.presentation.scanner.ScannerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    // Scanner
    viewModel { ScannerViewModel(get()) }

    // Generator
    viewModel { GeneratorViewModel(get()) }

    // Creation
    viewModel { CreationViewModel(androidContext(), get()) }

    // Code Detail
    viewModel { CodeDetailViewModel(androidContext(), get()) }

    // History
    viewModel { HistoryViewModel(get(), get()) }
}
