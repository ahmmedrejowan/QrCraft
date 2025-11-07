package com.rejown.qrcraft.di

import androidx.room.Room
import com.rejown.qrcraft.data.local.database.QRCraftDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            QRCraftDatabase::class.java,
            "qrcraft_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<QRCraftDatabase>().scanHistoryDao() }
    single { get<QRCraftDatabase>().generatedCodeDao() }
}
