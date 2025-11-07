package com.rejown.qrcraft.di

import com.rejown.qrcraft.presentation.generator.GeneratorViewModel
import com.rejown.qrcraft.presentation.history.HistoryViewModel
import com.rejown.qrcraft.presentation.scanner.ScannerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    // Scanner
    viewModel { ScannerViewModel(get()) }

    // Generator
    viewModel { GeneratorViewModel(get()) }

    // History
    viewModel { HistoryViewModel(get(), get()) }
}
