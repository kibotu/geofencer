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

        result.lastLocation?.let { LocationTracker.mutableLocations.tryEmit(it) }
        LocationTracker.mutableRawResults.tryEmit(result)

        val actionClass = context.getSharedPreferences(Prefs.LOCATION_PREFS, Context.MODE_PRIVATE)
            .getString(Prefs.LOCATION_ACTION_KEY, "")
        if (actionClass.isNullOrEmpty()) return

        Workers.enqueueLocationAction(context, actionClass, intent.toUri(0))
    }

    companion object {
        internal const val ACTION_PROCESS_UPDATES =
            "net.kibotu.geofencer.internal.ACTION_PROCESS_UPDATES"
    }
}
