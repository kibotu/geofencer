package net.kibotu.geofencer.internal

import android.location.Location

internal class ProviderQualityFilter(
    private val minAccuracyForNetworkProvider: Float = 50f,
) : LocationFilter {
    override fun accept(candidate: Location, history: LocationHistory): Boolean {
        if (minAccuracyForNetworkProvider <= 0f) return true
        if (candidate.provider == "network" && candidate.hasAccuracy() &&
            candidate.accuracy > minAccuracyForNetworkProvider
        ) {
            return false
        }
        return true
    }
}
