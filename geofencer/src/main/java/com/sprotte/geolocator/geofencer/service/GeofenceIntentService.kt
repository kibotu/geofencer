package com.sprotte.geolocator.geofencer.service

import android.content.Intent
import androidx.core.app.JobIntentService
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.geofencer.models.Geofence
import com.sprotte.geolocator.utils.log

abstract class GeofenceIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        log("onHandleWork $intent")
        val geofence =
            Geofencer.parseExtras(applicationContext, intent)
        if (geofence != null) {
            onGeofence(geofence)
        }
    }

    abstract fun onGeofence(geofence: Geofence)
}