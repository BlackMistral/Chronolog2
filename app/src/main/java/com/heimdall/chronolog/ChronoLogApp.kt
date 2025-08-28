package com.heimdall.chronolog

import android.app.Application
import com.jakewharton.timber.Timber
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe Application personalizzata.
 * È il punto di ingresso dell'app e viene usata per inizializzare
 * librerie a livello di applicazione come Hilt e Timber.
 *
 * L'annotazione @HiltAndroidApp è FONDAMENTALE. Attiva la generazione
 * del codice di Hilt per l'intera applicazione.
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
