package com.heimdall.chronolog

import android.app.Application
import timber.log.Timber

class ChronoLogApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
