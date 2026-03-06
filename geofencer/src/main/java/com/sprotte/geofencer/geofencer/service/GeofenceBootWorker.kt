package com.sprotte.geofencer.geofencer.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sprotte.geofencer.geofencer.Geofencer
import timber.log.Timber

class GeofenceBootWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            Geofencer(applicationContext).repository.reAddAll()
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "GeofenceBootWorker failed")
            Result.failure()
        }
    }
}
