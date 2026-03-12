package net.kibotu.geofencer.internal

import android.os.SystemClock
import net.kibotu.geofencer.Geofence
import net.kibotu.geofencer.GeofenceEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class EventDeduplicator(private val cooldown: Duration = 60.seconds) {

    private val lastEmitted = mutableMapOf<String, Pair<Geofence.Transition, Long>>()

    fun shouldEmit(event: GeofenceEvent, now: Long = SystemClock.elapsedRealtime()): Boolean {
        val key = event.geofence.id
        val last = lastEmitted[key]
        if (last != null && last.first == event.transition &&
            now - last.second < cooldown.inWholeMilliseconds
        ) {
            return false
        }
        lastEmitted[key] = event.transition to now
        return true
    }

    fun remove(geofenceId: String) {
        lastEmitted.remove(geofenceId)
    }

    fun clear() {
        lastEmitted.clear()
    }
}
