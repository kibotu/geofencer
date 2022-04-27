package com.sprotte.geolocator.geofencer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sprotte.geolocator.geofencer.GeofenceRepository
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.utils.log


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (context == null) {
            return
        }

        if (intent == null) {
            return
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
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
        log("unknow geofencing error" + geofencingEvent.triggeringGeofences.size)
        if (geofencingEvent.triggeringGeofences.size <= 0) return
        val repo = GeofenceRepository(context)
        log("unknow geofencing error" + repo.getAll().count())
        log("unknow geofencing error" + repo.getAll().first().id)
        log("unknow geofencing error" + geofencingEvent.triggeringGeofences[0].requestId)
        val geofence = repo.get(geofencingEvent.triggeringGeofences[0].requestId) ?: return

        //                    val i = Intent(context, Class.forName(geofence.intentClassName))
        //                    i.putExtra(Geofencer.INTENT_EXTRAS_KEY, geofence.id)
        //                    context.startService(i)
        //                    val clasz =Class.forName(geofence.intentClassName) as GeofenceIntentService
        ////                    JobIntentService.enqueueWork(context, clasz, 12345, i)

        log("geofence enqeue work geofence=$geofence")
        JobIntentService.enqueueWork(
            context,
            Class.forName(geofence.intentClassName),
            12345,
            Intent().apply {
                putExtra(Geofencer.INTENT_EXTRAS_KEY, geofence.id)
            })
    }
}