package net.kibotu.geofencer.geofencer.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import net.kibotu.geofencer.geofencer.Geofencer
import net.kibotu.geofencer.geofencer.models.CoreWorkerModule
import net.kibotu.geofencer.geofencer.models.GeoFenceUpdateModule
import net.kibotu.geofencer.geofencer.models.Geofence
import timber.log.Timber

@Suppress("UNCHECKED_CAST")
class GeoFenceUpdateWorker(val ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private fun startWorker(geoFence: Geofence) {
        try {
            val clazz: Class<*> = Class.forName(geoFence.intentClassName)
            if (!GeoFenceUpdateModule::class.java.isAssignableFrom(clazz)) return

            val moduleClass = clazz as Class<out CoreWorkerModule>
            val obj = moduleClass.constructors[0].newInstance(applicationContext)
            if (obj !is GeoFenceUpdateModule) return

            obj.onGeofence(geoFence)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start worker for geofence ${geoFence.id}")
        }
    }

    override fun doWork(): Result {
        return try {
            val geoFenceId = inputData.getString(Geofencer.INTENT_EXTRAS_KEY) ?: return Result.failure()
            Geofencer(ctx).get(geoFenceId)?.run { startWorker(this) }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "GeoFenceUpdateWorker failed")
            Result.failure()
        }
    }
}
