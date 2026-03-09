package net.kibotu.geofencer.internal

import android.content.Context
import androidx.startup.Initializer
import net.kibotu.geofencer.Geofencer

internal class GeofencerInitializer : Initializer<Geofencer> {

    override fun create(context: Context): Geofencer {
        Geofencer.init(context.applicationContext)
        return Geofencer
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
