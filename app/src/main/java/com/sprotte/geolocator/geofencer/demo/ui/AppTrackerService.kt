package com.sprotte.geolocator.geofencer.demo.ui

import com.exozet.android.core.gson.toJson
import com.exozet.android.core.storage.sharedPreference
import com.google.android.gms.location.LocationResult
import com.sprotte.geolocator.geofencer.demo.sendNotification
import com.sprotte.geolocator.tracking.service.LocationTrackerUpdateIntentService

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