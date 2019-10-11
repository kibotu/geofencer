package com.sprotte.geolocator.tracking.service

import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.LocationResult


abstract class LocationTrackerUpdateIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        onLocationResult(LocationResult.extractResult(intent) ?: return)
    }

    abstract fun onLocationResult(locationResult: LocationResult)
}