package com.sprotte.geolocator.geofencer.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.geofencer.models.CoreWorkerModule
import com.sprotte.geolocator.geofencer.models.GeoFenceUpdateModule
import com.sprotte.geolocator.geofencer.models.Geofence


@Suppress("UNCHECKED_CAST")
class GeoFenceUpdateWorker(val ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private fun startWorker(geoFence: Geofence) {
        try {
            val clazz: Class<*> = Class.forName(geoFence.intentClassName)
            if (!GeoFenceUpdateModule::class.java.isAssignableFrom(clazz)) {
                Result.failure()
                return
            }
            val moduleClass = clazz as Class<out CoreWorkerModule>
            val obj = moduleClass.constructors[0].newInstance(applicationContext)
            if (obj !is GeoFenceUpdateModule) {
                Result.failure()
                return
            }
            obj.onGeofence(geoFence)
        } catch (e: Exception) {
            Result.failure()
        }

    }

    override fun doWork(): Result {
        return try {
            val geoFenceId = inputData.getString(Geofencer.INTENT_EXTRAS_KEY) ?: return Result.failure()
            Geofencer(ctx).get(geoFenceId)?.run { startWorker(this) }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}


