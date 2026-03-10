package net.kibotu.geofencer.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult
import net.kibotu.geofencer.LocationTracker
import timber.log.Timber

class LocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        if (ACTION_PROCESS_UPDATES != intent.action) return

        val result = try {
            LocationResult.extractResult(intent) ?: return
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract location result")
            return
        }

        val prefs = context.getSharedPreferences(Prefs.LOCATION_PREFS, Context.MODE_PRIVATE)
        val maxAccuracy = prefs.getFloat(Prefs.LOCATION_MAX_ACCURACY_KEY, 0f)

        val location = result.lastLocation
        if (location != null) {
            if (maxAccuracy > 0f && location.hasAccuracy() && location.accuracy > maxAccuracy) {
                Timber.d("Dropping inaccurate location: %.1fm > %.1fm", location.accuracy, maxAccuracy)
            } else {
                LocationTracker.mutableLocations.tryEmit(location)
            }
        }

        LocationTracker.mutableRawResults.tryEmit(result)

        val actionClass = prefs.getString(Prefs.LOCATION_ACTION_KEY, "")
        if (actionClass.isNullOrEmpty()) return

        if (maxAccuracy > 0f && location != null && location.hasAccuracy() && location.accuracy > maxAccuracy) return

        Workers.enqueueLocationAction(context, actionClass, intent.toUri(0))
    }

    companion object {
        internal const val ACTION_PROCESS_UPDATES =
            "net.kibotu.geofencer.internal.ACTION_PROCESS_UPDATES"
    }
}
