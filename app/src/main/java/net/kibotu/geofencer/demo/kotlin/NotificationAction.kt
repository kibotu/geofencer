package net.kibotu.geofencer.demo.kotlin

import android.content.Context
import net.kibotu.geofencer.demo.misc.sendNotification
import net.kibotu.geofencer.geofencer.GeofenceAction
import net.kibotu.geofencer.geofencer.GeofenceEvent
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

        if (event.hasTriggeringLocation) {
            val marker = BreachMarker(
                latitude = event.triggeringLatitude,
                longitude = event.triggeringLongitude,
                geofenceId = event.geofence.id,
                geofenceLabel = event.geofence.label,
                transition = event.transition.name,
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
