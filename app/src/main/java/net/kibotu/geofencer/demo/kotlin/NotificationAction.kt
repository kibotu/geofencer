package net.kibotu.geofencer.demo.kotlin

import android.content.Context
import net.kibotu.geofencer.demo.misc.sendNotification
import net.kibotu.geofencer.GeofenceAction
import net.kibotu.geofencer.GeofenceEvent
import timber.log.Timber

class NotificationAction : GeofenceAction() {

    override fun onTriggered(context: Context, event: GeofenceEvent) {
        Timber.d("onTriggered $event")
        val transitionLabel = event.transition.name.uppercase()
        val title = if (event.geofence.label.isNotEmpty()) {
            "[$transitionLabel] ${event.geofence.label}"
        } else {
            "Geofence $transitionLabel"
        }

        val location = event.triggeringLocation
        if (location != null) {
            val marker = BreachMarker(
                latitude = location.latitude,
                longitude = location.longitude,
                geofenceId = event.geofence.id,
                geofenceLabel = event.geofence.label,
                transition = event.transition.name,
                geofenceLatitude = event.geofence.latitude,
                geofenceLongitude = event.geofence.longitude,
                geofenceRadius = event.geofence.radius,
            )
            BreachMarkerRepository.add(context, marker)
            Timber.d("Persisted breach marker at ${marker.latitude}, ${marker.longitude}")
        }

        sendNotification(
            context = context,
            title = title,
            message = event.geofence.message,
        )
    }
}
