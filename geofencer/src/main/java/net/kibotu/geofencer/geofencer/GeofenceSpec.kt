package net.kibotu.geofencer.geofencer

import net.kibotu.geofencer.geofencer.models.Geofence
import java.util.UUID
import kotlin.time.Duration

@DslMarker
annotation class GeofencerDsl

@GeofencerDsl
class GeofenceSpec {
    var id: String = UUID.randomUUID().toString()
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var radius: Double = 0.0
    var label: String = ""
    var message: String = ""
    var transitions: Int = Geofence.Transition.Enter.value
    var loiteringDelay: Duration? = null
    var responsiveness: Duration? = null
    @PublishedApi internal var actionClass: String = ""

    inline fun <reified T : GeofenceAction> action() {
        actionClass = T::class.java.canonicalName ?: ""
    }

    internal fun build(): Geofence = Geofence(
        id = id,
        latitude = latitude,
        longitude = longitude,
        radius = radius,
        label = label,
        message = message,
        transitions = transitions,
        actionClass = actionClass,
        loiteringDelayMillis = loiteringDelay?.inWholeMilliseconds ?: -1,
        responsivenessMillis = responsiveness?.inWholeMilliseconds ?: -1,
    )
}
