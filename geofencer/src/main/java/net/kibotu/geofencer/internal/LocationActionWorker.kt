package net.kibotu.geofencer.internal

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationResult
import net.kibotu.geofencer.LocationAction
import timber.log.Timber

internal class LocationActionWorker(
    ctx: Context,
    params: WorkerParameters,
) : Worker(ctx, params) {

    override fun doWork(): Result {
        return try {
            val intentUri = inputData.getString(Extras.LOCATION_INTENT) ?: return Result.failure()
            val className = inputData.getString(Extras.LOCATION_ACTION_CLASS) ?: return Result.failure()
            val intent = Intent.parseUri(intentUri, 0)
            val result = LocationResult.extractResult(intent) ?: return Result.failure()
            dispatch(result, className)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "LocationActionWorker failed")
            Result.failure()
        }
    }

    private fun dispatch(result: LocationResult, className: String) {
        try {
            val clazz = Class.forName(className)
            if (!LocationAction::class.java.isAssignableFrom(clazz)) return

            val action = clazz.getDeclaredConstructor().newInstance() as LocationAction
            action.onUpdate(applicationContext, result)
        } catch (e: Exception) {
            Timber.e(e, "Failed to dispatch location action: $className")
        }
    }
}
