package com.sprotte.geofencer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class GeofenceBootReceiver  : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED == intent.action) {
            GeofenceBootService.enqueueWork(
                context!!,
                intent
            )
        }
    }

}