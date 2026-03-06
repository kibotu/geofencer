package net.kibotu.geofencer.geofencer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import net.kibotu.geofencer.geofencer.GeofenceEvent
import net.kibotu.geofencer.geofencer.Geofencer
import net.kibotu.geofencer.geofencer.models.Geofence.Transition
import net.kibotu.geofencer.utils.enqueueOneTimeWorkRequest
import timber.log.Timber

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Timber.e("Geofencing error code: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Timber.d("Geofence transition: $geofenceTransition")

        val transition = Transition.of(geofenceTransition)
        if (transition == null) {
            Timber.w("Unknown geofence transition: $geofenceTransition")
            return
        }

        val triggeringGeofences = geofencingEvent.triggeringGeofences
        if (triggeringGeofences.isNullOrEmpty()) return

        val repo = Geofencer.getRepository(context)
        val geofence = repo.get(triggeringGeofences[0].requestId) ?: return

        val event = GeofenceEvent(geofence, transition)
        Geofencer.mutableEvents.tryEmit(event)

        Timber.d("Enqueuing work for geofence=${geofence.id}, actionClass=${geofence.actionClass}")
        if (geofence.actionClass.isNotEmpty()) {
            enqueueOneTimeWorkRequest(context, geofence.id)
        }
    }
}
