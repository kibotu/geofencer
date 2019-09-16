package com.sprotte.geolocator.demo.kotlin

import android.util.Log
import com.exozet.android.core.gson.toJson
import com.exozet.android.core.storage.sharedPreference
import com.google.android.gms.location.LocationResult
import com.sprotte.geolocator.demo.java.GeofenceIntentService
import com.sprotte.geolocator.demo.misc.sendNotification
import com.sprotte.geolocator.tracking.service.LocationTrackerUpdateIntentService

class LocationTrackerService : LocationTrackerUpdateIntentService() {

    private var location by sharedPreference(PREFERENCE_LOCATION, "")

    override fun onLocationResult(locationResult: LocationResult) {

        Log.v(GeofenceIntentService::class.java.simpleName, "onLocationResult $location")
        location = locationResult.toJson()
        sendNotification(
            applicationContext,
            "LOCATION",
            location
        )
    }

    companion object {
        const val PREFERENCE_LOCATION = "preference_location"
    }
}