package com.sprotte.geolocator.geofencer.demo.ui

import com.sprotte.geolocator.geofencer.demo.sendNotification
import com.sprotte.geolocator.geofencer.models.Geofence
import com.sprotte.geolocator.geofencer.service.GeofenceIntentService


class AppGeofenceService : GeofenceIntentService() {

    override fun onGeofence(geofence: Geofence) {
        sendNotification(applicationContext, geofence.title, geofence.message)
    }
}