package com.sprotte.geofencer.demo.ui

import android.content.Intent
import androidx.core.app.JobIntentService
import com.sprotte.geofencer.Geofencer
import com.sprotte.geofencer.demo.sendNotification
import com.sprotte.geofencer.log

class GeoFenceIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        log("GeoFenceIntentService onHandleWork: $intent")
        val id = intent.extras?.getString(Geofencer.INTENT_EXTRAS_KEY)
        log("GeoFenceIntentService onHandleWork: ${intent.extras.toString()}")
        log("GeoFenceIntentService onHandleWork: $id")
        if(id != null){
            val geofencer = Geofencer(applicationContext).get(id)
            log("GeoFenceIntentService onHandleWork: $geofencer")
            if(geofencer != null){
                sendNotification(applicationContext,geofencer.title,geofencer.message)
            }
        }

    }
}