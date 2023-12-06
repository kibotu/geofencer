/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */
@file:JvmName("DebugExtensions")

package com.sprotte.geolocator.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.geofencer.service.GeoFenceUpdateWorker
import com.sprotte.geolocator.geofencer.service.GeofenceBootWorker
import com.sprotte.geolocator.tracking.service.LocationTrackerUpdateWorker

internal val debug = true

internal fun Any.log(message: String?) {
    if (debug)
        Log.d(this::class.java.simpleName, "$message")
}

internal fun Any.loge(message: String?) {
    if (debug)
        Log.e(this::class.java.simpleName, "$message")
}

internal fun Throwable.log() {
    if (debug)
        Log.d(this::class.java.simpleName, "$message")
}


internal val gson by lazy {
    com.google.gson.GsonBuilder()
        .disableHtmlEscaping()
        .create()
}

internal inline fun <reified T> String.fromJson(): T = gson.fromJson<T>(this, T::class.java)

internal fun Any.toJson(): String = gson.toJson(this)

internal inline fun <reified T> Gson.fromJson(json: String): T =
    this.fromJson<T>(json, object : TypeToken<T>() {}.type)


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
    StartGameDialogFragment(rationalMessage, block).show(supportFragmentManager, "twoButtonDialog")
}

class StartGameDialogFragment(val rationalMessage: String, val block: (Boolean) -> Unit = { }) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(rationalMessage)
                .setPositiveButton(
                    com.sprotte.geolocator.R.string.button_allow
                ) { _, _ ->
                    block(true)
                }
                .setNegativeButton(
                    com.sprotte.geolocator.R.string.button_reject
                ) { _, _ ->
                    block(false)
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

fun enqueueOneTimeWorkRequest(ctx: Context, geoFenceId: String) {
    val inputData: Data = Data.Builder()
        .putString(Geofencer.INTENT_EXTRAS_KEY, geoFenceId)
        .build()
    val ontTimeWorkRequest = OneTimeWorkRequestBuilder<GeoFenceUpdateWorker>()
        .setInputData(inputData)
        .addTag(GeoFenceUpdateWorker::class.qualifiedName.toString())
        .build()
    WorkManager.getInstance(ctx).enqueue(ontTimeWorkRequest)
}

fun enqueueOneTimeBootWorkRequest(ctx: Context) {
    val ontTimeWorkRequest = OneTimeWorkRequestBuilder<GeofenceBootWorker>()
        .addTag(GeoFenceUpdateWorker::class.qualifiedName.toString())
        .build()
    WorkManager.getInstance(ctx).enqueue(ontTimeWorkRequest)
}

fun enqueueOneTimeLocationUpdateWorkRequest(ctx: Context, componentName: String, intent: Intent) {
    val inputData: Data = Data.Builder()
        .putString(Geofencer.LOCATION_UPDATE_CLASS_NAME, componentName)
        .putString(Geofencer.LOCATION_UPDATE_CLASS_NAME, Gson().toJson(intent))
        .build()

    val ontTimeWorkRequest = OneTimeWorkRequestBuilder<LocationTrackerUpdateWorker>()
        .setInputData(inputData)
        .addTag(GeoFenceUpdateWorker::class.qualifiedName.toString())
        .build()
    WorkManager.getInstance(ctx).enqueue(ontTimeWorkRequest)
}
