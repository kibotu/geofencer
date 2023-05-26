package com.sprotte.geolocator.demo.kotlin

import android.content.Context
import com.sprotte.geolocator.demo.misc.sendNotification
import com.sprotte.geolocator.geofencer.models.CoreWorkerModule
import com.sprotte.geolocator.geofencer.models.Geofence
import timber.log.Timber

class NotificationWorker (private val context: Context): CoreWorkerModule(context){
    override fun onGeofence(geofence: Geofence) {
        Timber.d("onGeofence $geofence")
        sendNotification(
            context = context,
            title = geofence.title,
            message = geofence.message
        )
    }

}