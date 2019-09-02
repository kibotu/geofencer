package com.sprotte.geofencer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sprotte.geofencer.utils.log

class GeofenceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        log("GeofenceBootReceiver: onReceive $intent")

        if (intent == null)
            return

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> enqueueWork(context, intent)
            else -> {
            }
        }
    }

    private fun enqueueWork(context: Context?, intent: Intent) {
        GeofenceBootService.enqueueWork(
            context!!,
            intent
        )
    }

}