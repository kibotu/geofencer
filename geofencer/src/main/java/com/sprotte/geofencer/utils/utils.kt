@file:JvmName("DebugExtensions")

package com.sprotte.geofencer.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sprotte.geofencer.geofencer.Geofencer
import com.sprotte.geofencer.geofencer.service.GeoFenceUpdateWorker
import com.sprotte.geofencer.geofencer.service.GeofenceBootWorker
import com.sprotte.geofencer.tracking.service.LocationTrackerUpdateWorker
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

fun FragmentActivity.showTwoButtonDialog(rationalMessage: String, block: (Boolean) -> Unit) {
    RationaleDialogFragment(rationalMessage, block).show(supportFragmentManager, "twoButtonDialog")
}

class RationaleDialogFragment(
    private val rationalMessage: String,
    private val block: (Boolean) -> Unit = { }
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setMessage(rationalMessage)
                .setPositiveButton(com.sprotte.geofencer.R.string.button_allow) { _, _ ->
                    block(true)
                }
                .setNegativeButton(com.sprotte.geofencer.R.string.button_reject) { _, _ ->
                    block(false)
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

fun enqueueOneTimeWorkRequest(ctx: Context, geoFenceId: String) {
    val inputData: Data = Data.Builder()
        .putString(Geofencer.INTENT_EXTRAS_KEY, geoFenceId)
        .build()
    val oneTimeWorkRequest = OneTimeWorkRequestBuilder<GeoFenceUpdateWorker>()
        .setInputData(inputData)
        .addTag(GeoFenceUpdateWorker::class.qualifiedName.toString())
        .build()
    WorkManager.getInstance(ctx).enqueue(oneTimeWorkRequest)
}

fun enqueueOneTimeBootWorkRequest(ctx: Context) {
    val oneTimeWorkRequest = OneTimeWorkRequestBuilder<GeofenceBootWorker>()
        .addTag(GeofenceBootWorker::class.qualifiedName.toString())
        .build()
    WorkManager.getInstance(ctx).enqueue(oneTimeWorkRequest)
}

fun enqueueOneTimeLocationUpdateWorkRequest(ctx: Context, componentName: String, intentJson: String) {
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
