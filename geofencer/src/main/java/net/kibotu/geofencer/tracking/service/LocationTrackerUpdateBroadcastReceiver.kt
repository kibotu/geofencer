package net.kibotu.geofencer.tracking.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult
import net.kibotu.geofencer.tracking.LocationTracker
import net.kibotu.geofencer.utils.enqueueOneTimeLocationUpdateWorkRequest
import net.kibotu.geofencer.utils.getSharedPrefs
import timber.log.Timber

class LocationTrackerUpdateBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Timber.d("onReceive $intent")

        if (intent == null) return
        if (ACTION_PROCESS_UPDATES != intent.action) return

        val result = try {
            LocationResult.extractResult(intent) ?: return
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract location result")
            return
        }

        Timber.d("result = $result")

        LocationTracker.mutableLocations.tryEmit(result)

        val actionClass = context.getSharedPrefs().getString(LocationTracker.PREFS_NAME, "")
        if (actionClass.isNullOrEmpty()) return

        enqueueOneTimeLocationUpdateWorkRequest(context, actionClass, intent.toUri(0))
    }

    companion object {
        internal const val ACTION_PROCESS_UPDATES =
            "net.kibotu.geofencer.tracking.service.ACTION_PROCESS_UPDATES"
    }
}
