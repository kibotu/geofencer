package net.kibotu.geofencer.tracking.service

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationResult
import net.kibotu.geofencer.geofencer.Geofencer
import net.kibotu.geofencer.geofencer.models.LocationTrackerUpdateModule
import timber.log.Timber

class LocationTrackerUpdateWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private fun startWorker(locationResult: LocationResult, clazzNameString: String) {
        val clazz: Class<*> = Class.forName(clazzNameString)
        if (!LocationTrackerUpdateModule::class.java.isAssignableFrom(clazz)) {
            return
        }
        @Suppress("UNCHECKED_CAST")
        val moduleClass = clazz as Class<out LocationTrackerUpdateModule>
        val obj = moduleClass.constructors[0].newInstance(applicationContext)
        if (obj !is LocationTrackerUpdateModule) {
            return
        }
        obj.onLocationResult(locationResult)
    }

    override fun doWork(): Result {
        return try {
            val intentUriString = inputData.getString(Geofencer.LOCATION_UPDATE_INTENT) ?: return Result.failure()
            val clazzName = inputData.getString(Geofencer.LOCATION_UPDATE_CLASS_NAME) ?: return Result.failure()
            val intent = Intent.parseUri(intentUriString, 0)
            val result = LocationResult.extractResult(intent) ?: return Result.failure()
            startWorker(result, clazzName)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "LocationTrackerUpdateWorker failed")
            Result.failure()
        }
    }
}
