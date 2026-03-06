package com.sprotte.geofencer.demo.kotlin

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationResult
import com.sprotte.geofencer.geofencer.models.LocationTrackerUpdateModule
import timber.log.Timber

class LocationTrackerWorker(context: Context) : LocationTrackerUpdateModule(context) {

    override fun onLocationResult(locationResult: LocationResult) {
        Timber.v("locationResult=$locationResult")
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(USER_LOCATION_KEY, locationResult.toString())
        }
    }

    companion object {
        const val USER_LOCATION_KEY = "user_location"
    }
}
