package net.kibotu.geofencer.geofencer.models

import net.kibotu.geofencer.geofencer.TransitionType
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Geofence(
    val id: String = UUID.randomUUID().toString(),
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var radius: Double = 0.0,
    var title: String = "",
    var message: String = "",
    @param:TransitionType
    var transitionType: Int = 1,
    var intentClassName: String = ""
)
