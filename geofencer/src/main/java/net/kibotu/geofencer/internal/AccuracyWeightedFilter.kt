package net.kibotu.geofencer.internal

import android.location.Location

internal class AccuracyWeightedFilter : LocationFilter {
    override fun accept(candidate: Location, history: LocationHistory): Boolean {
        val prev = history.last() ?: return true
        if (!prev.hasAccuracy() || !candidate.hasAccuracy()) return true
        val distance = prev.distanceTo(candidate)
        return distance >= (prev.accuracy + candidate.accuracy) * 0.5f
    }
}
