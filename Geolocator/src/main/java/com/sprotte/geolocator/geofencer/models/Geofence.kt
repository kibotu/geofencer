package com.sprotte.geolocator.geofencer.models

import com.sprotte.geolocator.geofencer.TransitionType
import java.util.UUID

data class Geofence(
    /**
     * uuid
     */
    val id: String = UUID.randomUUID().toString(),
    /**
     * latitude in degrees, between -90 and +90 inclusive
     */
    var latitude: Double = 0.0,
    /**
     * longitude in degrees, between -180 and +180 inclusive
     */
    var longitude: Double = 0.0,
    /**
     * Radius in meters.
     */
    var radius: Double = 0.0,
    /**
     * title
     */
    var title: String = "",
    /**
     * message
     */
    var message: String = "",
    /**
     * [com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL]
     * [com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER]
     * [com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT]
     */
    @TransitionType
    var transitionType: Int = 1
) {
    /**
     * Event Receiver. Needs to be [com.sprotte.geolocator.geofencer.models.CoreWorkerModule]
     */
    var intentClassName: String = ""
}