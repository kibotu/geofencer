package net.kibotu.geofencer.demo.kotlin

import android.content.Context
import net.kibotu.geofencer.demo.misc.sendNotification
import net.kibotu.geofencer.geofencer.models.GeoFenceUpdateModule
import net.kibotu.geofencer.geofencer.models.Geofence
import timber.log.Timber

class NotificationWorker(context: Context) : GeoFenceUpdateModule(context) {
    override fun onGeofence(geofence: Geofence) {
        Timber.d("onGeofence $geofence")
        sendNotification(
            context = context,
            title = geofence.title,
            message = geofence.message
        )
    }
}
