package com.heimdall.chronolog

import android.app.Application
import com.jakewharton.timber.Timber
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe Application personalizzata.
 *
 * L'annotazione @HiltAndroidApp è FONDAMENTALE. Attiva la generazione
 * del codice di Hilt e definisce questa classe come punto di ingresso
 * per la dependency injection.
 */
@HiltAndroidApp
class ChronoLogApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inizializza Timber per il logging, ma solo nelle build di debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
