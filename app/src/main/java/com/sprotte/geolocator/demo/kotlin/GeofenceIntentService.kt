package com.sprotte.geolocator.demo.kotlin

import android.util.Log
import com.sprotte.geolocator.demo.misc.sendNotification
import com.sprotte.geolocator.geofencer.models.Geofence


class GeofenceIntentService : com.sprotte.geolocator.geofencer.service.GeofenceIntentService() {

    override fun onGeofence(geofence: Geofence) {
        Log.v(com.sprotte.geolocator.demo.java.GeofenceIntentService::class.java.simpleName, "onGeofence $geofence")
        sendNotification(
            applicationContext,
            geofence.title,
            geofence.message
        )
    }
}