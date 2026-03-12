package net.kibotu.geofencer.internal

import android.location.Location
import net.kibotu.geofencer.Geofence
import net.kibotu.geofencer.GeofenceEvent
import net.kibotu.geofencer.LatLng

internal class GeofenceConsistencyEvaluator {

    private val counters = mutableMapOf<String, TransitionCounter>()

    fun evaluate(location: Location, geofences: List<Geofence>): List<GeofenceEvent> {
        val events = mutableListOf<GeofenceEvent>()
        for (geofence in geofences) {
            val distance = distanceTo(location, geofence)
            val inside = distance <= geofence.radius
            val counter = counters.getOrPut(geofence.id) {
                TransitionCounter(geofence.consistentSamples)
            }
            val transition = counter.update(inside) ?: continue
            if (transition in geofence.transitions) {
                events += GeofenceEvent(
                    geofence = geofence,
                    transition = transition,
                    triggeringLocation = LatLng(location.latitude, location.longitude),
                )
            }
        }
        return events
    }

    fun remove(geofenceId: String) {
        counters.remove(geofenceId)
    }

    fun clear() {
        counters.clear()
    }
}

private fun distanceTo(location: Location, geofence: Geofence): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        location.latitude, location.longitude,
        geofence.latitude, geofence.longitude,
        results,
    )
    return results[0]
}

internal class TransitionCounter(private val requiredSamples: Int = 3) {

    private var believedInside: Boolean? = null
    private var consecutiveOpposite = 0

    fun update(inside: Boolean): Geofence.Transition? {
        if (believedInside == null) {
            believedInside = inside
            return null
        }
        if (inside == believedInside) {
            consecutiveOpposite = 0
            return null
        }
        consecutiveOpposite++
        if (consecutiveOpposite >= requiredSamples) {
            believedInside = inside
            consecutiveOpposite = 0
            return if (inside) Geofence.Transition.Enter else Geofence.Transition.Exit
        }
        return null
    }
}
