package com.sprotte.geolocator.geofencer.models

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.sprotte.geolocator.geofencer.service.GeoFenceWorkerInterface

open class WorkerModule: Service(), GeoFenceWorkerInterface {


    var ctx: Context? = null

    companion object {
        const val WORKER_MODULE_INTENT = "com.sprotte.geolocator.worker.WORKER_MODULE_INTENT"
    }
    override fun onGeofence(geofence: Geofence) {
        // no-op
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Can not bind to this service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        throw UnsupportedOperationException("Can not start this service")
    }

}