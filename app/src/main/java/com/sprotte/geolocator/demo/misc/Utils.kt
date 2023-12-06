package com.sprotte.geolocator.demo.misc

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sprotte.geolocator.demo.BuildConfig
import com.sprotte.geolocator.demo.R
import com.sprotte.geolocator.demo.kotlin.MainActivity
import com.sprotte.geolocator.geofencer.models.Geofence
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.Disposable

fun EditText.requestFocusWithKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    if (!hasFocus()) {
        requestFocus()
    }

    post { imm.showSoftInput(this, InputMethodManager.SHOW_FORCED) }
}

fun hideKeyboard(context: Context, view: View) {
    val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    keyboard.hideSoftInputFromWindow(view.windowToken, 0)
}

fun vectorToBitmap(resources: Resources, @DrawableRes id: Int): BitmapDescriptor {
    val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable!!.intrinsicWidth,
        vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun showGeofenceInMap(
    context: Context,
    map: GoogleMap,
    geofence: Geofence
) {

    val latLng = LatLng(geofence.latitude, geofence.longitude)
    val vectorToBitmap = vectorToBitmap(
        context.resources,
        R.drawable.ic_twotone_location_on_48px
    )
    val marker = map.addMarker(MarkerOptions().position(latLng).icon(vectorToBitmap))
    marker?.tag = geofence.id
    val radius = geofence.radius
    map.addCircle(
        CircleOptions()
            .center(latLng)
            .radius(radius)
            .strokeColor(ContextCompat.getColor(context,
                R.color.colorAccent
            ))
            .fillColor(ContextCompat.getColor(context,
                R.color.colorReminderFill
            ))
    )

}

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, title: String, message: String) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context.applicationContext, MainActivity::class.java)

    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(MainActivity::class.java)
        .addNextIntent(intent)
    val notificationPendingIntent = stackBuilder
        .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val notification = NotificationCompat.Builder(context,
        NOTIFICATION_CHANNEL_ID
    )
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(message)
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(getUniqueId(), notification)
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())

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


fun FragmentActivity.requestLocationPermission(block: (permission: Permission) -> Unit): () -> Unit {
    val mutableList = mutableListOf<Disposable?>()
    mutableList.add(
        requestForegroundLocationPermission {
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                // user did not grant this permission
                block(it)
                return@requestForegroundLocationPermission
            }
            // now requesting background permission
            mutableList.add(
                requestBackgroundLocationPermission(block)
            )
        }
    )

    // lambda to close all disposables
    val cancelDisposable: () -> Unit = {
        mutableList.forEach {
            it?.run {
                dispose()
            }
        }
    }
    return cancelDisposable
}



fun FragmentActivity.requestForegroundLocationPermission(block: (permission: Permission) -> Unit): Disposable? = RxPermissions(this)
    .requestEachCombined(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,

    )
    .subscribe({
        block(it)
    }, {
        Log.v("LocationPermission", "location permission $it")
    })

fun FragmentActivity.requestBackgroundLocationPermission(block: (permission: Permission) -> Unit): Disposable? = RxPermissions(this)
    .requestEachCombined(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,

        )
    .subscribe({
        block(it)
    }, {
        Log.v("LocationPermission", "location permission $it")
    })


