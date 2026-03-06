package com.sprotte.geofencer.tracking

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.sprotte.geofencer.tracking.service.LocationTrackerUpdateBroadcastReceiver
import com.sprotte.geofencer.tracking.service.LocationTrackerUpdateIntentService
import com.sprotte.geofencer.utils.getSharedPrefs
import timber.log.Timber

object LocationTracker {

    private const val REQUEST_CODE = 5998
    internal val PREFS_NAME = LocationTrackerUpdateIntentService::class.java.canonicalName

    private fun getTrackingPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, LocationTrackerUpdateBroadcastReceiver::class.java)
        intent.action = LocationTrackerUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            flags
        )
    }

    private fun getLocationRequest(
        context: Context,
        params: LocationTrackerParams = LocationTrackerParams(context)
    ): LocationRequest {
        return LocationRequest.Builder(
            params.priority,
            params.interval
        ).apply {
            setMinUpdateIntervalMillis(params.fastestInterval)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setMaxUpdateDelayMillis(params.maxWaitTime)
            setWaitForAccurateLocation(true)
            setMinUpdateDistanceMeters(params.smallestDisplacement)
        }.build()
    }

    fun requestLocationUpdates(context: Context, clasz: Class<*>) {
        requestLocationUpdates(context.applicationContext, clasz, LocationTrackerParams(context))
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(
        context: Context,
        clasz: Class<*>,
        params: LocationTrackerParams = LocationTrackerParams(context)
    ) {
        context.getSharedPrefs().edit().putString(PREFS_NAME, clasz.canonicalName).apply()
        try {
            Timber.d("Starting location updates")
            LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(
                getLocationRequest(context, params),
                getTrackingPendingIntent(context)
            )
        } catch (e: SecurityException) {
            Timber.e(e, "Missing location permission")
        }
    }

    fun removeLocationUpdates(context: Context) {
        Timber.d("Removing location updates")
        LocationServices.getFusedLocationProviderClient(context)
            .removeLocationUpdates(getTrackingPendingIntent(context))
    }
}
