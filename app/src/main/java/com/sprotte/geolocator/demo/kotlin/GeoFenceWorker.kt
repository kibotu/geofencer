package com.sprotte.geolocator.demo.kotlin

import com.sprotte.geolocator.demo.misc.sendNotification
import com.sprotte.geolocator.geofencer.models.Geofence
import com.sprotte.geolocator.geofencer.models.WorkerModule
import timber.log.Timber

class GeoFenceWorker: WorkerModule() {

    override fun onGeofence(geofence: Geofence) {
        Timber.d("onGeofence $geofence")
        ctx?.run {
            sendNotification(
                context = this,
                geofence.title,
                geofence.message
            )
        }


    }
}