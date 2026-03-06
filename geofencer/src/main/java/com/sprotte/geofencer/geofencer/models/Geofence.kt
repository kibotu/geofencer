package com.sprotte.geofencer.geofencer.models

import com.sprotte.geofencer.geofencer.TransitionType
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
    @TransitionType
    var transitionType: Int = 1,
    var intentClassName: String = ""
)
