package net.kibotu.geofencer

import android.content.Context
import androidx.annotation.Keep
import com.google.android.gms.location.LocationResult

@Keep
abstract class LocationAction {
    abstract fun onUpdate(context: Context, result: LocationResult)
}
