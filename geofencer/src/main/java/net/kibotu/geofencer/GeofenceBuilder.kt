package net.kibotu.geofencer

import java.util.UUID
import kotlin.time.Duration

@DslMarker
annotation class GeofencerDsl

@GeofencerDsl
class GeofenceBuilder {
    var id: String = UUID.randomUUID().toString()
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var radius: Double = 0.0
    var label: String = ""
    var message: String = ""
    var transitions: Set<Geofence.Transition> = setOf(Geofence.Transition.Enter)
    var loiteringDelay: Duration = Duration.ZERO
    var responsiveness: Duration = Duration.ZERO
    var expiration: Duration = Duration.INFINITE
    @PublishedApi internal var actionClass: String = ""

    inline fun <reified T : GeofenceAction> action() {
        actionClass = T::class.java.canonicalName ?: ""
    }

    internal fun build(): Geofence {
        require(radius > 0.0) { "Radius must be positive, was $radius" }
        require(latitude in -90.0..90.0) { "Latitude must be in [-90, 90], was $latitude" }
        require(longitude in -180.0..180.0) { "Longitude must be in [-180, 180], was $longitude" }
        require(transitions.isNotEmpty()) { "At least one transition must be specified" }
        return Geofence(
            id = id,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            label = label,
            message = message,
            transitions = transitions,
            actionClass = actionClass,
            loiteringDelay = loiteringDelay,
            responsiveness = responsiveness,
            expiration = expiration,
        )
    }
}
