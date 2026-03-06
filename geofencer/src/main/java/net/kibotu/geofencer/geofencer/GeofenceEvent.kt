package net.kibotu.geofencer.geofencer

import net.kibotu.geofencer.geofencer.models.Geofence

data class GeofenceEvent(
    val geofence: Geofence,
    val transition: Geofence.Transition,
)
