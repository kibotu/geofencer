package com.sprotte.geolocator.tracking.service

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.geofencer.models.GeoFenceUpdateModule
import com.sprotte.geolocator.geofencer.models.LocationTrackerUpdateModule
import com.sprotte.geolocator.utils.fromJson

class LocationTrackerUpdateWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private fun startWorker(locationResult: LocationResult, clazzNameString: String) {
        val clazz: Class<*> = Class.forName(clazzNameString)
        if (!GeoFenceUpdateModule::class.java.isAssignableFrom(clazz)) {
            Result.failure()
            return
        }
        val moduleClass = clazz as Class<out LocationTrackerUpdateModule>
        val obj = moduleClass.constructors[0].newInstance(applicationContext)
        if (obj !is LocationTrackerUpdateModule) {
            Result.failure()
            return
        }
        obj.onLocationResult(locationResult)
    }

    override fun doWork(): Result {
        return try {
            val intentString = inputData.getString(Geofencer.LOCATION_UPDATE_INTENT) ?: return Result.failure()
            val clazzName = inputData.getString(Geofencer.LOCATION_UPDATE_CLASS_NAME) ?: return Result.failure()
            val intent: Intent = Gson().fromJson(intentString)
            val result = LocationResult.extractResult(intent) ?: return Result.failure()
            startWorker(result, clazzName)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}