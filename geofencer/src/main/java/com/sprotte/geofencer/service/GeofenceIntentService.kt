package com.sprotte.geofencer.service

import android.content.Intent
import androidx.core.app.JobIntentService
import com.sprotte.geofencer.Geofencer
import com.sprotte.geofencer.models.Geofence
import com.sprotte.geofencer.utils.log

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