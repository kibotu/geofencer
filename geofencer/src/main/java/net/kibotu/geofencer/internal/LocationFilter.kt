package net.kibotu.geofencer.internal

import android.location.Location

internal fun interface LocationFilter {
    fun accept(candidate: Location, history: LocationHistory): Boolean
}

internal class LocationFilterPipeline(
    private val filters: List<LocationFilter>,
) : LocationFilter {
    override fun accept(candidate: Location, history: LocationHistory): Boolean =
        filters.all { it.accept(candidate, history) }
}
