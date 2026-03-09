package net.kibotu.geofencer

data class GeofenceEvent(
    val geofence: Geofence,
    val transition: Geofence.Transition,
    val triggeringLocation: LatLng? = null,
)
