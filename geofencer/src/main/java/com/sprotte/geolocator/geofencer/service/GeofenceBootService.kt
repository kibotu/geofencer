package com.sprotte.geolocator.geofencer.service

import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull
import androidx.core.app.JobIntentService
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.utils.log

class GeofenceBootService : JobIntentService() {

    companion object {

        private const val JOB_ID = 1066
        fun enqueueWork(context: Context, work: Intent) {
            log("GeofenceBootService: enqueueWork")
            enqueueWork(
                context, GeofenceBootService::class.java,
                JOB_ID, work
            )
        }
    }

    override fun onHandleWork(@NonNull intent: Intent) {
        log("GeofenceBootService: onHandleWork")
        Geofencer(applicationContext).repository.reAddAll()
    }

}