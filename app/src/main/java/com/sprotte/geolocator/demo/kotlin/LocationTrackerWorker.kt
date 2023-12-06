package com.sprotte.geolocator.demo.kotlin

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.android.gms.location.LocationResult
import com.sprotte.geolocator.demo.misc.toJson
import com.sprotte.geolocator.geofencer.models.LocationTrackerUpdateModule
import timber.log.Timber

class LocationTrackerWorker(context: Context) : LocationTrackerUpdateModule(context) {

    override fun onLocationResult(locationResult: LocationResult) {

        Timber.v("locationResult=$locationResult")

        sharedPreferences?.edit {
            putString(USER_LOCATION_KEY, locationResult.toJson())
        }
    }

    companion object {
        const val USER_LOCATION_KEY = "user_location"
    }
}
