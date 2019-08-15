package com.sprotte.geofencer.demo.ui

import android.app.IntentService
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.sprotte.geofencer.Geofencer
import com.sprotte.geofencer.demo.sendNotification
import com.sprotte.geofencer.log

class GeoFenceIntentService : IntentService("GeoFenceIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        log("GeoFenceIntentService onHandleWork: $intent")
        if(intent == null)
            return

        val id = intent.extras?.getString(Geofencer.INTENT_EXTRAS_KEY)
        if(id != null){
            val geofencer = Geofencer(applicationContext).get(id)
            if(geofencer != null){
                sendNotification(applicationContext,geofencer.title,geofencer.message)
            }
        }
    }
}