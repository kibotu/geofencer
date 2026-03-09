package net.kibotu.geofencer.internal

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import net.kibotu.geofencer.Geofencer
import timber.log.Timber

internal class BootWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            Geofencer.getRepository(applicationContext).reAddAll()
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "BootWorker failed")
            Result.failure()
        }
    }
}
