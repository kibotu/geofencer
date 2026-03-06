package net.kibotu.geofencer.geofencer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.kibotu.geofencer.utils.enqueueOneTimeBootWorkRequest
import timber.log.Timber

class GeofenceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d("GeofenceBootReceiver: onReceive $intent")

        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> context?.let { enqueueOneTimeBootWorkRequest(it) }
        }
    }
}
