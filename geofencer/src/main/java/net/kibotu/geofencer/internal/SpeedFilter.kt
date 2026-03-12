package net.kibotu.geofencer.internal

import android.location.Location

internal class SpeedFilter(private val maxSpeedMps: Float = 55f) : LocationFilter {
    override fun accept(candidate: Location, history: LocationHistory): Boolean {
        if (maxSpeedMps <= 0f) return true
        val prev = history.last() ?: return true
        val dt = (candidate.elapsedRealtimeNanos - prev.elapsedRealtimeNanos) / 1_000_000_000.0
        if (dt <= 0.0) return false
        val speed = prev.distanceTo(candidate) / dt
        return speed <= maxSpeedMps
    }
}
