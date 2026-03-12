package net.kibotu.geofencer.internal

import android.location.Location

internal fun location(
    lat: Double,
    lng: Double,
    accuracy: Float = 10f,
    provider: String = "gps",
    elapsedNanos: Long = 0L,
): Location = Location(provider).apply {
    latitude = lat
    longitude = lng
    this.accuracy = accuracy
    elapsedRealtimeNanos = elapsedNanos
    time = elapsedNanos / 1_000_000
}
