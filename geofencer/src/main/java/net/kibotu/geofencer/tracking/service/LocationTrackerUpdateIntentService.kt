package net.kibotu.geofencer.tracking.service

import com.google.android.gms.location.LocationResult

abstract class LocationTrackerUpdateIntentService {

    abstract fun onLocationResult(locationResult: LocationResult)
}
