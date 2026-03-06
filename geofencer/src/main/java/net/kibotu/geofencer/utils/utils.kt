@file:JvmName("DebugExtensions")

package net.kibotu.geofencer.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import net.kibotu.geofencer.geofencer.Geofencer
import net.kibotu.geofencer.geofencer.service.GeoFenceUpdateWorker
import net.kibotu.geofencer.geofencer.service.GeofenceBootWorker
import net.kibotu.geofencer.tracking.service.LocationTrackerUpdateWorker
import timber.log.Timber

internal fun Any.log(message: String?) {
    Timber.tag(this::class.java.simpleName).d(message)
}

internal fun Any.loge(message: String?) {
    Timber.tag(this::class.java.simpleName).e(message)
}

internal fun <T> lazyFast(operation: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    operation()
}

internal fun Context.safeContext(): Context =
    takeUnless {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> isDeviceProtectedStorage
            else -> true
        }
    }?.run {
        applicationContext.let {
            ContextCompat.createDeviceProtectedStorageContext(it) ?: it
        }
    } ?: this

internal fun Context.getSharedPrefs(): SharedPreferences {
    val safeContext: Context by lazyFast { this.safeContext() }
    val sharedPreferences: SharedPreferences by lazyFast {
        PreferenceManager.getDefaultSharedPreferences(safeContext)
    }
    return sharedPreferences
}

internal fun Context.getRes(resInt: Int): Long {
    return applicationContext.resources.getInteger(resInt).toLong()
}

internal fun enqueueOneTimeWorkRequest(
    ctx: Context,
    geoFenceId: String,
    transitionType: Int = -1,
    triggeringLatitude: Double = Double.NaN,
    triggeringLongitude: Double = Double.NaN,
) {
    val inputData: Data = Data.Builder()
        .putString(Geofencer.INTENT_EXTRAS_KEY, geoFenceId)
        .putInt(Geofencer.EXTRA_TRANSITION_TYPE, transitionType)
        .putDouble(Geofencer.EXTRA_TRIGGERING_LAT, triggeringLatitude)
        .putDouble(Geofencer.EXTRA_TRIGGERING_LNG, triggeringLongitude)
        .build()
    val oneTimeWorkRequest = OneTimeWorkRequestBuilder<GeoFenceUpdateWorker>()
        .setInputData(inputData)
        .addTag(GeoFenceUpdateWorker::class.qualifiedName.toString())
        .build()
    WorkManager.getInstance(ctx).enqueue(oneTimeWorkRequest)
}

internal fun enqueueOneTimeBootWorkRequest(ctx: Context) {
    val oneTimeWorkRequest = OneTimeWorkRequestBuilder<GeofenceBootWorker>()
        .addTag(GeofenceBootWorker::class.qualifiedName.toString())
        .build()
    WorkManager.getInstance(ctx).enqueue(oneTimeWorkRequest)
}

internal fun enqueueOneTimeLocationUpdateWorkRequest(ctx: Context, componentName: String, intentJson: String) {
    val inputData: Data = Data.Builder()
        .putString(Geofencer.LOCATION_UPDATE_CLASS_NAME, componentName)
        .putString(Geofencer.LOCATION_UPDATE_INTENT, intentJson)
        .build()
    val oneTimeWorkRequest = OneTimeWorkRequestBuilder<LocationTrackerUpdateWorker>()
        .setInputData(inputData)
        .addTag(LocationTrackerUpdateWorker::class.qualifiedName.toString())
        .build()
    WorkManager.getInstance(ctx).enqueue(oneTimeWorkRequest)
}
