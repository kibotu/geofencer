package com.sprotte.geofencer.service

import android.app.IntentService
import android.content.Intent
import com.sprotte.geofencer.Geofencer
import com.sprotte.geofencer.models.Geofence

abstract class GeofenceIntentService : IntentService("GeofenceIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null)
            return

        val geofence =
            Geofencer.parseExtras(applicationContext, intent)
        if (geofence != null) {
            onGeofence(geofence)
        }
    }

    abstract fun onGeofence(geofence: Geofence)
}