/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */
@file:JvmName("DebugExtensions")

package com.sprotte.geolocator.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.preference.PreferenceManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sprotte.geolocator.BuildConfig

internal val debug = BuildConfig.DEBUG

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

internal fun Context.getSharedPrefs(): SharedPreferences{
    val safeContext: Context by lazyFast { this.safeContext() }

    val sharedPreferences: SharedPreferences by lazyFast {
        PreferenceManager.getDefaultSharedPreferences(safeContext)
    }
    return sharedPreferences
}


internal fun Context.getRes(resInt: Int): Long{
    return applicationContext.resources.getInteger(resInt).toLong()
}