package com.sprotte.geolocator.geofencer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sprotte.geolocator.geofencer.GeofenceRepository
import com.sprotte.geolocator.utils.enqueueOneTimeWorkRequest
import com.sprotte.geolocator.utils.log


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (context == null) {
            return
        }

        if (intent == null) {
            return
        }

        var geofencingEvent: GeofencingEvent? = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            return
        }

        if (geofencingEvent.hasError()) {
            log("geofencing errorCode: $geofencingEvent.errorCode")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        log("geo        fence was triggered: $geofenceTransition")
        if (geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER && geofenceTransition != Geofence.GEOFENCE_TRANSITION_EXIT) {
            log("unknow geofencing error")
            return
        }
        log("unknow geofencing error" + geofencingEvent.triggeringGeofences?.size)
        if ((geofencingEvent.triggeringGeofences?.size ?: 0) <= 0) return
        val repo = GeofenceRepository(context)

        log("unknow geofencing error" + repo.getAll().count())
        log("unknow geofencing error" + repo.getAll().firstOrNull()?.id)
        log("unknow geofencing error" + geofencingEvent.triggeringGeofences?.get(0)?.requestId)
        val geofence = repo.get(geofencingEvent.triggeringGeofences?.get(0)?.requestId) ?: return

        log("geofence enqeue work geofence=$geofence")
        log("geofence enqeue work geofence=$geofence intentClassName=${geofence.intentClassName}")
        enqueueOneTimeWorkRequest(context, geofence.id)
    }
}