package net.kibotu.geofencer.geofencer

import net.kibotu.geofencer.geofencer.models.Geofence

data class GeofenceEvent(
    val geofence: Geofence,
    val transition: Geofence.Transition,
    val triggeringLatitude: Double = Double.NaN,
    val triggeringLongitude: Double = Double.NaN,
) {
    val hasTriggeringLocation: Boolean
        get() = !triggeringLatitude.isNaN() && !triggeringLongitude.isNaN()
}
