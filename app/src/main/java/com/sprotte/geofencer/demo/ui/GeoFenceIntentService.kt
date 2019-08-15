package com.sprotte.geofencer.demo.ui

import android.app.IntentService
import android.content.Intent
import com.sprotte.geofencer.Geofencer
import com.sprotte.geofencer.demo.sendNotification


class GeoFenceIntentService : IntentService("GeoFenceIntentService") {

    override fun onHandleIntent(intent: Intent?) {
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