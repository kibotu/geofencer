package net.kibotu.geofencer

import com.google.android.gms.location.Priority
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@GeofencerDsl
class LocationConfig {
    var interval: Duration = 10.seconds
    var fastest: Duration = 5.seconds
    var maxDelay: Duration = 30.seconds
    var displacement: Float = 0f
    var priority: Int = Priority.PRIORITY_HIGH_ACCURACY

    /** Drop location updates with accuracy worse (higher) than this value in meters. 0 = no filter. */
    var maxAccuracyMeters: Float = 0f

    /** Max plausible speed in m/s. Updates implying faster movement are rejected. 0 = disabled. */
    var maxSpeedMps: Float = 55f

    /** Reject updates that fall within the combined accuracy radius of the previous fix. */
    var accuracyWeightedFilterEnabled: Boolean = true

    /** Reject network-provider locations with accuracy worse than this. 0 = disabled. */
    var minNetworkProviderAccuracy: Float = 50f

    @PublishedApi internal var actionClass: String = ""

    inline fun <reified T : LocationAction> action() {
        actionClass = T::class.java.canonicalName ?: ""
    }
}
