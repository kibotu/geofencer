package net.kibotu.geofencer

import kotlinx.serialization.Serializable

@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double,
)
