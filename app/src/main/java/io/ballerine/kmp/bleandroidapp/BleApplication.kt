package io.ballerine.kmp.bleandroidapp

import android.app.Application
import timber.log.Timber

class BleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
