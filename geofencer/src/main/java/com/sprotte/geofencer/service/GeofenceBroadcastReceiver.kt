package com.sprotte.geofencer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import androidx.core.app.JobIntentService
import com.sprotte.geofencer.GeofenceRepository
import com.sprotte.geofencer.Geofencer
import com.sprotte.geofencer.utils.log


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if(context == null){
            return
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) {
            log("geofencing errorCode: $geofencingEvent.errorCode")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        log("geofence was triggered: $geofenceTransition")
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            if (geofencingEvent.triggeringGeofences.size > 0) {
                val repo = GeofenceRepository(context)
                val geofence = repo.get(geofencingEvent.triggeringGeofences[0].requestId)
                if (geofence != null) {
                    val i = Intent(context,  Class.forName(geofence.intentClassName))
                    i.putExtra(Geofencer.INTENT_EXTRAS_KEY,geofence.id)
                    context.startService(i)
                    Class.forName(geofence.intentClassName).newInstance()
                    JobIntentService.enqueueWork(context, Class.forName(geofence.intentClassName), 12345, i)
                }
            }
        } else {
            log("unknow geofencing error")
        }
    }
}