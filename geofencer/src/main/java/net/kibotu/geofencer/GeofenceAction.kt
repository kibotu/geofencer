package net.kibotu.geofencer

import android.content.Context
import androidx.annotation.Keep

@Keep
abstract class GeofenceAction {
    abstract fun onTriggered(context: Context, event: GeofenceEvent)
}
