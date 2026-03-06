package net.kibotu.geofencer.tracking

import android.content.Context
import com.google.android.gms.location.LocationResult

abstract class LocationAction {
    abstract fun onUpdate(context: Context, result: LocationResult)
}
