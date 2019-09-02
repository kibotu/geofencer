package com.sprotte.geofencer.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.annotation.NonNull
import com.sprotte.geofencer.Geofencer

class GeofenceBootService : JobIntentService() {

    companion object {

        private const val JOB_ID = 1066
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, GeofenceBootService::class.java,
                JOB_ID, work)
        }
    }

    override fun onHandleWork(@NonNull intent: Intent) {
        Geofencer(applicationContext).repository.reAddAll()
    }

}