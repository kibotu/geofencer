package net.kibotu.geofencer.geofencer

import android.content.Context

abstract class GeofenceAction {
    abstract fun onTriggered(context: Context, event: GeofenceEvent)
}
