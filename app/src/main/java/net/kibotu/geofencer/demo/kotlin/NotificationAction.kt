package net.kibotu.geofencer.demo.kotlin

import android.content.Context
import net.kibotu.geofencer.demo.misc.sendNotification
import net.kibotu.geofencer.geofencer.GeofenceAction
import net.kibotu.geofencer.geofencer.GeofenceEvent
import timber.log.Timber

class NotificationAction : GeofenceAction() {

    override fun onTriggered(context: Context, event: GeofenceEvent) {
        Timber.d("onTriggered $event")
        sendNotification(
            context = context,
            title = event.geofence.label,
            message = event.geofence.message,
        )
    }
}
