package net.kibotu.geofencer.geofencer.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import net.kibotu.geofencer.geofencer.Geofencer
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
