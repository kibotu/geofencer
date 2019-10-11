package com.sprotte.geolocator.demo.kotlin

import android.util.Log
import com.exozet.android.core.storage.sharedPreference
import com.google.android.gms.location.LocationResult
import com.sprotte.geolocator.demo.java.GeofenceIntentService
import com.sprotte.geolocator.tracking.service.LocationTrackerUpdateIntentService

class LocationTrackerService : LocationTrackerUpdateIntentService() {

    private var location by sharedPreference(PREFERENCE_LOCATION, LocationResult.create(listOf()))

    override fun onLocationResult(locationResult: LocationResult) {

        Log.v(GeofenceIntentService::class.java.simpleName, "onLocationResult $location")

        // store location result to shared preferences
        location = locationResult

        // retrieve location data:
        // val lastLocation = locationResult.lastLocation
        // val locations = locationResult.locations
        // locations.forEach {
        //     it.latitude
        //     it.longitude
        //     it.altitude
        //     it.speed
        //     it.bearing
        // }

        // lastLocation.latitude
        // lastLocation.longitude
        // lastLocation.altitude
        // lastLocation.speed
        // lastLocation.bearing

        // send push local push notification
        // sendNotification(
        //     applicationContext,
        //     "LOCATION",
        //     location
        // )
    }

    companion object {
        const val PREFERENCE_LOCATION = "preference_location"
    }
}