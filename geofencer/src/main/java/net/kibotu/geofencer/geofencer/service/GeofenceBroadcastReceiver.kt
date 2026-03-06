package net.kibotu.geofencer.geofencer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import net.kibotu.geofencer.geofencer.GeofenceRepository
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

        if (geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER &&
            geofenceTransition != Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            Timber.w("Unknown geofence transition: $geofenceTransition")
            return
        }

        val triggeringGeofences = geofencingEvent.triggeringGeofences
        if (triggeringGeofences.isNullOrEmpty()) return

        val repo = GeofenceRepository(context)
        val geofence = repo.get(triggeringGeofences[0].requestId) ?: return

        Timber.d("Enqueuing work for geofence=${geofence.id}, intentClassName=${geofence.intentClassName}")
        enqueueOneTimeWorkRequest(context, geofence.id)
    }
}
