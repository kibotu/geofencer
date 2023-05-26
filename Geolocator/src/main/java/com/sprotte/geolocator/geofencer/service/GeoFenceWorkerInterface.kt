package com.sprotte.geolocator.geofencer.service

import androidx.annotation.WorkerThread
import com.sprotte.geolocator.geofencer.models.Geofence

interface GeoFenceWorkerInterface {
    @WorkerThread
    fun onGeofence(geofence: Geofence)
}