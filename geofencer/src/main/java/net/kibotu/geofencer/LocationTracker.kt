package net.kibotu.geofencer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.kibotu.geofencer.internal.LocationReceiver
import net.kibotu.geofencer.internal.Prefs
import timber.log.Timber

object LocationTracker {

    private const val REQUEST_CODE = 5998

    internal val mutableLocations = MutableSharedFlow<Location>(extraBufferCapacity = 64)
    internal val mutableRawResults = MutableSharedFlow<LocationResult>(extraBufferCapacity = 64)

    val locations: SharedFlow<Location> = mutableLocations.asSharedFlow()

    fun start(context: Context, block: LocationConfig.() -> Unit = {}): Result<Unit> = runCatching {
        val config = LocationConfig().apply(block)
        val appContext = context.applicationContext

        appContext.getSharedPreferences(Prefs.LOCATION_PREFS, Context.MODE_PRIVATE)
            .edit()
            .apply {
                if (config.actionClass.isNotEmpty()) {
                    putString(Prefs.LOCATION_ACTION_KEY, config.actionClass)
                }
                putFloat(Prefs.LOCATION_MAX_ACCURACY_KEY, config.maxAccuracyMeters)
            }
            .apply()

        val request = LocationRequest.Builder(config.priority, config.interval.inWholeMilliseconds)
            .setMinUpdateIntervalMillis(config.fastest.inWholeMilliseconds)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .setMaxUpdateDelayMillis(config.maxDelay.inWholeMilliseconds)
            .setWaitForAccurateLocation(true)
            .setMinUpdateDistanceMeters(config.displacement)
            .build()

        Timber.d("Starting location updates")
        LocationServices.getFusedLocationProviderClient(appContext)
            .requestLocationUpdates(request, pendingIntent(appContext))
    }

    fun stop(context: Context) {
        Timber.d("Stopping location updates")
        val appContext = context.applicationContext
        LocationServices.getFusedLocationProviderClient(appContext)
            .removeLocationUpdates(pendingIntent(appContext))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, LocationReceiver::class.java)
        intent.action = LocationReceiver.ACTION_PROCESS_UPDATES
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }
}
