package com.sprotte.geofencer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import android.content.ComponentName


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val LOG_TAG = "GeofenceReceiver"

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
                    val intent = Intent()
                    intent.extras?.putString(Geofencer.INTENT_EXTRAS_KEY,geofence.id)
                    intent.setPackage(context.packageName)
                    intent.setClassName(context.packageName, geofence.intentClassName)
                    log("created intent: $intent")
                    context.startService(intent)
                }
            }
        } else {
            log("unknow geofencing error")
        }
    }

    fun createExplicitFromImplicitIntent(context: Context, implicitIntent: Intent): Intent? {
        //Retrieve all services that can match the given intent
        val pm = context.packageManager
        val resolveInfo = pm.queryIntentServices(implicitIntent, 0)

        //Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size != 1) {
            return null
        }

        //Get component info and create ComponentName
        val serviceInfo = resolveInfo[0]
        val packageName = serviceInfo.serviceInfo.packageName
        val className = serviceInfo.serviceInfo.name
        val component = ComponentName(packageName, className)

        //Create a new intent. Use the old one for extras and such reuse
        val explicitIntent = Intent(implicitIntent)

        //Set the component to be explicit
        explicitIntent.component = component

        return explicitIntent
    }
}