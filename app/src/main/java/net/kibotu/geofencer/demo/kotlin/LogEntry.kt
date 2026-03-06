package net.kibotu.geofencer.demo.kotlin

import com.google.android.gms.location.LocationResult
import net.kibotu.geofencer.geofencer.GeofenceEvent
import net.kibotu.geofencer.geofencer.models.Geofence
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    .withZone(ZoneId.systemDefault())

sealed class LogEntry(val timestamp: Instant) {

    abstract val icon: String
    abstract val title: String
    abstract val detail: String

    class Location(
        val result: LocationResult,
        ts: Instant = Instant.now(),
    ) : LogEntry(ts) {

        override val icon: String = "\uD83D\uDCCD"

        override val title: String
            get() {
                val loc = result.lastLocation ?: return "—"
                return "%.6f, %.6f".format(loc.latitude, loc.longitude)
            }

        override val detail: String
            get() {
                val loc = result.lastLocation ?: return timeFmt.format(timestamp)
                return "\u00B1%.1fm \u00B7 %s".format(loc.accuracy, timeFmt.format(timestamp))
            }
    }

    class Fence(
        val event: GeofenceEvent,
        ts: Instant = Instant.now(),
    ) : LogEntry(ts) {

        override val icon: String
            get() = when (event.transition) {
                Geofence.Transition.Enter -> "\u2B07\uFE0F"
                Geofence.Transition.Exit -> "\u2B06\uFE0F"
                Geofence.Transition.Dwell -> "\u23F3"
            }

        override val title: String
            get() {
                val label = event.geofence.label.ifEmpty { event.geofence.id.take(8) }
                return "${event.transition.name}: $label"
            }

        override val detail: String
            get() = "${event.geofence.message.ifEmpty { "—" }} \u00B7 ${timeFmt.format(timestamp)}"
    }
}
