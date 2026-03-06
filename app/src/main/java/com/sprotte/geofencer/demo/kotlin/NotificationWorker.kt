package com.sprotte.geofencer.demo.kotlin

import android.content.Context
import com.sprotte.geofencer.demo.misc.sendNotification
import com.sprotte.geofencer.geofencer.models.GeoFenceUpdateModule
import com.sprotte.geofencer.geofencer.models.Geofence
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
