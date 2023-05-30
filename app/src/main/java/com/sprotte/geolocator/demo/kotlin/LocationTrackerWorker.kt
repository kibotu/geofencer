package com.sprotte.geolocator.demo.kotlin

import android.content.Context
import com.google.android.gms.location.LocationResult
import com.sprotte.geolocator.demo.misc.sharedPreference
import com.sprotte.geolocator.demo.misc.toJson
import com.sprotte.geolocator.geofencer.models.LocationTrackerUpdateModule

class LocationTrackerWorker (context: Context): LocationTrackerUpdateModule(context){
    private var location by sharedPreference(PREFERENCE_LOCATION, "")

    companion object {
        const val PREFERENCE_LOCATION = "preference_location"
    }
    override fun onLocationResult(locationResult: LocationResult) {
        // store location result to shared preferences
        location = locationResult.toJson()
    }

}