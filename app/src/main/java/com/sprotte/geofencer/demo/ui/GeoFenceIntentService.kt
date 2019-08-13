package com.sprotte.geofencer.demo.ui

import android.content.Intent
import androidx.core.app.JobIntentService
import com.sprotte.geofencer.Geofencer
import com.sprotte.geofencer.demo.sendNotification

class GeoFenceIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {

        val id = intent.extras?.getString(Geofencer.INTENT_EXTRAS_KEY)
        if(id != null){
            val geofencer = Geofencer(applicationContext).get(id)
            if(geofencer != null){
                sendNotification(applicationContext,geofencer.title,geofencer.message)
            }
        }

    }
}