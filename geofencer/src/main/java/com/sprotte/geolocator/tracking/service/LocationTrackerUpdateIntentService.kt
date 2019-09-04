package com.sprotte.geolocator.tracking.service

import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.LocationResult
import com.sprotte.geolocator.utils.log

abstract class LocationTrackerUpdateIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        log("onHandleWork $intent")
        val locationResult =
            LocationResult.extractResult(intent)
        if (locationResult != null) {
            onLocationResult(locationResult)
        }
    }

    abstract fun onLocationResult(locationResult: LocationResult)
}