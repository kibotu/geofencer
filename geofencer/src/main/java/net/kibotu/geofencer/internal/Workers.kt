package net.kibotu.geofencer.internal

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

internal object Workers {

    fun enqueueGeofenceAction(
        ctx: Context,
        geofenceId: String,
        transitionType: Int = -1,
        triggeringLatitude: Double = Double.NaN,
        triggeringLongitude: Double = Double.NaN,
    ) {
        val inputData = Data.Builder()
            .putString(Extras.GEOFENCE_ID, geofenceId)
            .putInt(Extras.TRANSITION_TYPE, transitionType)
            .putDouble(Extras.TRIGGERING_LAT, triggeringLatitude)
            .putDouble(Extras.TRIGGERING_LNG, triggeringLongitude)
            .build()
        val request = OneTimeWorkRequestBuilder<GeofenceActionWorker>()
            .setInputData(inputData)
            .addTag(GeofenceActionWorker::class.qualifiedName.toString())
            .build()
        WorkManager.getInstance(ctx).enqueue(request)
    }

    fun enqueueBoot(ctx: Context) {
        val request = OneTimeWorkRequestBuilder<BootWorker>()
            .addTag(BootWorker::class.qualifiedName.toString())
            .build()
        WorkManager.getInstance(ctx).enqueue(request)
    }

    fun enqueueLocationAction(ctx: Context, className: String, intentUri: String) {
        val inputData = Data.Builder()
            .putString(Extras.LOCATION_ACTION_CLASS, className)
            .putString(Extras.LOCATION_INTENT, intentUri)
            .build()
        val request = OneTimeWorkRequestBuilder<LocationActionWorker>()
            .setInputData(inputData)
            .addTag(LocationActionWorker::class.qualifiedName.toString())
            .build()
        WorkManager.getInstance(ctx).enqueue(request)
    }
}
