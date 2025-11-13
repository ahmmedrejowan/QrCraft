package com.rejown.qrcraft.di

import androidx.room.Room
import com.rejown.qrcraft.data.local.database.QRCraftDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    // Room Database
    single {
        timber.log.Timber.tag("QC DatabaseModule").d("init - Creating Room database")
        val db = Room.databaseBuilder(
            androidContext(),
            QRCraftDatabase::class.java,
            "qrcraft_database"
        )
            .addMigrations(com.rejown.qrcraft.data.local.database.MIGRATION_1_2)
            .fallbackToDestructiveMigration(false)
            .build()
        timber.log.Timber.tag("QC DatabaseModule").d("init - Room database created successfully")
        db
    }

    // DAOs
    single {
        timber.log.Timber.tag("QC DatabaseModule").d("init - Creating ScanHistoryDao")
        get<QRCraftDatabase>().scanHistoryDao()
    }
    single {
        timber.log.Timber.tag("QC DatabaseModule").d("init - Creating GeneratedCodeDao")
        get<QRCraftDatabase>().generatedCodeDao()
    }
}
