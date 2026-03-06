package net.kibotu.geofencer.geofencer.models

import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Geofence(
    val id: String = UUID.randomUUID().toString(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Double = 0.0,
    val label: String = "",
    val message: String = "",
    val transitions: Int = Transition.Enter.value,
    internal val actionClass: String = "",
    val loiteringDelayMillis: Long = -1,
    val responsivenessMillis: Long = -1,
) {

    enum class Transition(val value: Int) {
        Enter(GEOFENCE_TRANSITION_ENTER),
        Exit(GEOFENCE_TRANSITION_EXIT),
        Dwell(GEOFENCE_TRANSITION_DWELL);

        operator fun plus(other: Transition): Int = value or other.value

        companion object {
            fun of(value: Int): Transition? = entries.find { it.value == value }
        }
    }
}
