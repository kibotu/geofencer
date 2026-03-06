package net.kibotu.geofencer.geofencer.service

import androidx.annotation.WorkerThread
import com.google.android.gms.location.LocationResult
import net.kibotu.geofencer.geofencer.models.Geofence

interface GeofencerInterface

interface GeoFenceBootInterface : GeofencerInterface {
    @WorkerThread
    fun readAllGeoFence()
}

interface GeoFenceUpdateInterface : GeofencerInterface {
    @WorkerThread
    fun onGeofence(geofence: Geofence)
}

interface LocationTrackerUpdateInterface : GeofencerInterface {
    @WorkerThread
    fun onLocationResult(locationResult: LocationResult)
}
