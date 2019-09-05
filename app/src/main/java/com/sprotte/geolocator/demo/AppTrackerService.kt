package com.sprotte.geolocator.demo

import com.exozet.android.core.gson.toJson
import com.exozet.android.core.storage.sharedPreference
import com.google.android.gms.location.LocationResult
import com.sprotte.geolocator.tracking.service.LocationTrackerUpdateIntentService
import net.kibotu.logger.Logger

class AppTrackerService : LocationTrackerUpdateIntentService() {

    private var location by sharedPreference(PREFERENCE_LOCATION, "")

    override fun onLocationResult(locationResult: LocationResult) {
        location = locationResult.toJson()
        sendNotification(applicationContext, "LOCATION", location)
    }

    companion object {
         const val PREFERENCE_LOCATION = "preference_location"
    }
}