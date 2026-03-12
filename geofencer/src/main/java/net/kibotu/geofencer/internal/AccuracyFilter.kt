package net.kibotu.geofencer.internal

import android.location.Location

internal class AccuracyFilter(private val maxMeters: Float) : LocationFilter {
    override fun accept(candidate: Location, history: LocationHistory): Boolean =
        maxMeters <= 0f || !candidate.hasAccuracy() || candidate.accuracy <= maxMeters
}
