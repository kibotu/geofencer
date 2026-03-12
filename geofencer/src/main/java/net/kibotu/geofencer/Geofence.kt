package net.kibotu.geofencer

import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Serializable
data class Geofence(
    val id: String = UUID.randomUUID().toString(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Double = 0.0,
    val label: String = "",
    val message: String = "",
    val transitions: Set<Transition> = setOf(Transition.Enter),
    internal val actionClass: String = "",
    @Serializable(with = DurationMillisSerializer::class)
    val loiteringDelay: kotlin.time.Duration = kotlin.time.Duration.ZERO,
    @Serializable(with = DurationMillisSerializer::class)
    val responsiveness: kotlin.time.Duration = kotlin.time.Duration.ZERO,
    @Serializable(with = DurationMillisSerializer::class)
    val expiration: kotlin.time.Duration = kotlin.time.Duration.INFINITE,
    val consistentSamples: Int = 3,
    @Serializable(with = DurationMillisSerializer::class)
    val enterDwellDuration: kotlin.time.Duration = 30.seconds,
    @Serializable(with = DurationMillisSerializer::class)
    val exitDwellDuration: kotlin.time.Duration = 30.seconds,
) {

    @Serializable
    enum class Transition(val value: Int) {
        Enter(GEOFENCE_TRANSITION_ENTER),
        Exit(GEOFENCE_TRANSITION_EXIT),
        Dwell(GEOFENCE_TRANSITION_DWELL);

        operator fun plus(other: Transition): Set<Transition> = setOf(this, other)

        companion object {
            fun of(value: Int): Transition? = entries.find { it.value == value }
        }
    }

    internal fun transitionBitmask(): Int =
        transitions.fold(0) { acc, t -> acc or t.value }
}
