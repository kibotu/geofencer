package net.kibotu.geofencer.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofencingEvent
import net.kibotu.geofencer.Geofence
import net.kibotu.geofencer.GeofenceEvent
import net.kibotu.geofencer.Geofencer
import net.kibotu.geofencer.LatLng
import timber.log.Timber

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Timber.e("Geofencing error code: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val transition = Geofence.Transition.of(geofenceTransition)
        if (transition == null) {
            Timber.w("Unknown geofence transition: $geofenceTransition")
            return
        }

        val triggeringGeofences = geofencingEvent.triggeringGeofences
        if (triggeringGeofences.isNullOrEmpty()) return

        val repo = Geofencer.getRepository(context)
        val triggeringLocation = geofencingEvent.triggeringLocation
        val latLng = triggeringLocation?.let { LatLng(it.latitude, it.longitude) }

        for (gmsGeofence in triggeringGeofences) {
            val geofence = repo.get(gmsGeofence.requestId) ?: continue

            val event = GeofenceEvent(
                geofence = geofence,
                transition = transition,
                triggeringLocation = latLng,
            )

            if (!GeofenceEvaluationState.deduplicator.shouldEmit(event)) continue

            Geofencer.mutableEvents.tryEmit(event)

            if (geofence.actionClass.isNotEmpty()) {
                Workers.enqueueGeofenceAction(
                    ctx = context,
                    geofenceId = geofence.id,
                    transitionType = geofenceTransition,
                    triggeringLatitude = triggeringLocation?.latitude ?: Double.NaN,
                    triggeringLongitude = triggeringLocation?.longitude ?: Double.NaN,
                )
            }
        }
    }
}
