package net.kibotu.geofencer.internal

import android.os.SystemClock
import net.kibotu.geofencer.Geofence
import net.kibotu.geofencer.GeofenceEvent

internal class DwellConfirmation {

    private val pending = mutableMapOf<String, PendingTransition>()

    fun process(event: GeofenceEvent, now: Long = SystemClock.elapsedRealtime()): GeofenceEvent? {
        if (event.transition == Geofence.Transition.Dwell) return event

        val requiredMs = when (event.transition) {
            Geofence.Transition.Enter -> event.geofence.enterDwellDuration
            Geofence.Transition.Exit -> event.geofence.exitDwellDuration
            Geofence.Transition.Dwell -> return event
        }.inWholeMilliseconds

        if (requiredMs <= 0) return event

        val current = pending[event.geofence.id]
        if (current != null && current.transition == event.transition) {
            if (now - current.startedAt >= requiredMs) {
                pending.remove(event.geofence.id)
                return event
            }
            return null
        }

        if (current != null && current.transition != event.transition) {
            pending.remove(event.geofence.id)
        }

        pending[event.geofence.id] = PendingTransition(event.transition, now)
        return null
    }

    fun remove(geofenceId: String) {
        pending.remove(geofenceId)
    }

    fun clear() {
        pending.clear()
    }
}

internal data class PendingTransition(
    val transition: Geofence.Transition,
    val startedAt: Long,
)
