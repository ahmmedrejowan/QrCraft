package com.rejown.qrcraft.di

import com.rejown.qrcraft.data.repository.GeneratorRepositoryImpl
import com.rejown.qrcraft.data.repository.ScanRepositoryImpl
import com.rejown.qrcraft.data.repository.SettingsRepositoryImpl
import com.rejown.qrcraft.domain.repository.GeneratorRepository
import com.rejown.qrcraft.domain.repository.ScanRepository
import com.rejown.qrcraft.domain.repository.SettingsRepository
import org.koin.dsl.module

val repositoryModule = module {
    // Repositories
    single<ScanRepository> { ScanRepositoryImpl(get()) }
    single<GeneratorRepository> { GeneratorRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}
