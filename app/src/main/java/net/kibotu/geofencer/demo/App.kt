package net.kibotu.geofencer.demo

import android.app.Application
import android.content.pm.ApplicationInfo
import net.kibotu.geofencer.geofencer.Geofencer
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            Timber.plant(Timber.DebugTree())
        }
        Geofencer.init(this)
        Timber.i("GeofencerDemo initialized")
    }
}
