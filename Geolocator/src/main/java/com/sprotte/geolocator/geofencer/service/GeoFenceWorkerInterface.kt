package com.sprotte.geolocator.geofencer.service

import com.sprotte.geolocator.geofencer.models.Geofence

interface GeoFenceWorkerInterface {
    fun onGeofence(geofence: Geofence)
}