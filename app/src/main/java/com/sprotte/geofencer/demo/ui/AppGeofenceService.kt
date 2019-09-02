package com.sprotte.geofencer.demo.ui

import com.sprotte.geofencer.service.GeofenceIntentService
import com.sprotte.geofencer.demo.sendNotification
import com.sprotte.geofencer.models.Geofence


class AppGeofenceService : GeofenceIntentService() {

    override fun onGeofence(geofence: Geofence) {

        sendNotification(applicationContext, geofence.title, geofence.message)
    }
}