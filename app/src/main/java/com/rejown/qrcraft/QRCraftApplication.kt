package com.rejown.qrcraft

import android.app.Application
import com.rejown.qrcraft.di.appModule
import com.rejown.qrcraft.di.databaseModule
import com.rejown.qrcraft.di.repositoryModule
import com.rejown.qrcraft.di.useCaseModule
import com.rejown.qrcraft.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class QRCraftApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@QRCraftApplication)
            modules(
                appModule,
                databaseModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )
        }

        Timber.d("QRCraft Application initialized")
    }
}
