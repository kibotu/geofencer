package com.sprotte.geofencer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val LOG_TAG = "GeofenceReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {

        if(context == null){
            return
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) {
            Log.e(LOG_TAG, "$geofencingEvent.errorCode")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            if (geofencingEvent.triggeringGeofences.size > 0) {
                val repo = GeofenceRepository(context)
                val geofence = repo.get(geofencingEvent.triggeringGeofences[0].requestId)
                if (geofence != null) {
                    val intent = Intent()
                    intent.extras?.putString(Geofencer.INTENT_EXTRAS_KEY,geofence.id)
                    intent.setPackage(context.packageName)
                    intent.setClassName(context.packageName, geofence.intentClassName)
                    context.startService(Intent())
                }
            }
        } else {
            Log.e(LOG_TAG, "Error")
        }
    }
}