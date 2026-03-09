package net.kibotu.geofencer.demo.kotlin

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationResult
import net.kibotu.geofencer.LocationAction
import timber.log.Timber

class LocationLogAction : LocationAction() {

    override fun onUpdate(context: Context, result: LocationResult) {
        Timber.v("locationResult=$result")
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(USER_LOCATION_KEY, result.toString())
        }
    }

    companion object {
        const val USER_LOCATION_KEY = "user_location"
    }
}
