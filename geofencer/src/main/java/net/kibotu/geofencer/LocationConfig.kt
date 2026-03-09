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
    @PublishedApi internal var actionClass: String = ""

    inline fun <reified T : LocationAction> action() {
        actionClass = T::class.java.canonicalName ?: ""
    }
}
