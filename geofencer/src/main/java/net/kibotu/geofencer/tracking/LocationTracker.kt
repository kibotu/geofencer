package net.kibotu.geofencer.tracking

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.kibotu.geofencer.tracking.service.LocationTrackerUpdateBroadcastReceiver
import net.kibotu.geofencer.utils.getSharedPrefs
import timber.log.Timber

object LocationTracker {

    private const val REQUEST_CODE = 5998
    internal const val PREFS_NAME = "net.kibotu.geofencer.tracking.LocationTracker"

    internal val mutableLocations = MutableSharedFlow<LocationResult>(extraBufferCapacity = 64)

    val locations: SharedFlow<LocationResult> = mutableLocations.asSharedFlow()

    fun start(context: Context, block: LocationSpec.() -> Unit = {}) {
        val spec = LocationSpec().apply(block)
        val appContext = context.applicationContext

        if (spec.actionClass.isNotEmpty()) {
            appContext.getSharedPrefs().edit()
                .putString(PREFS_NAME, spec.actionClass)
                .apply()
        }

        val request = LocationRequest.Builder(spec.priority, spec.interval.inWholeMilliseconds)
            .setMinUpdateIntervalMillis(spec.fastest.inWholeMilliseconds)
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setMaxUpdateDelayMillis(spec.maxDelay.inWholeMilliseconds)
            .setWaitForAccurateLocation(true)
            .setMinUpdateDistanceMeters(spec.displacement)
            .build()

        try {
            Timber.d("Starting location updates")
            LocationServices.getFusedLocationProviderClient(appContext)
                .requestLocationUpdates(request, pendingIntent(appContext))
        } catch (e: SecurityException) {
            Timber.e(e, "Missing location permission")
        }
    }

    fun stop(context: Context) {
        Timber.d("Stopping location updates")
        val appContext = context.applicationContext
        LocationServices.getFusedLocationProviderClient(appContext)
            .removeLocationUpdates(pendingIntent(appContext))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, LocationTrackerUpdateBroadcastReceiver::class.java)
        intent.action = LocationTrackerUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }
}
