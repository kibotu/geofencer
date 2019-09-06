package com.sprotte.geolocator.geofencer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence.Builder
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.sprotte.geolocator.geofencer.models.Geofence
import com.sprotte.geolocator.geofencer.service.GeofenceBroadcastReceiver
import com.sprotte.geolocator.utils.fromJson
import com.sprotte.geolocator.utils.loge
import com.sprotte.geolocator.utils.sharedPreference
import com.sprotte.geolocator.utils.toJson

class GeofenceRepository(private val context: Context) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent
    get() {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            Geofencer.REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    var geofenceString by context.sharedPreference(Geofencer.PREFS_NAME, "")

    fun add(
        geofence: Geofence,
        success: () -> Unit) {

        val androidGeofence = buildGeofence(geofence)
        geofencingClient
            .addGeofences(buildGeofencingRequest(androidGeofence), geofencePendingIntent )
            .addOnSuccessListener {
                geofenceString = (getAll() + geofence).toJson()
                success()
            }
            .addOnFailureListener {
                loge(it.message)
            }
    }

    fun remove(
        geofence: Geofence,
        success: () -> Unit) {
        val list = getAll() - geofence
        geofenceString = list.toJson()
        success()
    }

    fun removeAll(
        success: () -> Unit) {
        geofencingClient?.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                geofenceString = ""
                success()
            }
            addOnFailureListener {
                loge(it.message)
            }
        }
    }

    fun get(requestId: String?) = getAll().firstOrNull { it.id == requestId }

    fun getAll(): List<Geofence> {
        if (geofenceString == "") {
            return listOf()
        }
        val arrayOfReminders = geofenceString.fromJson<Array<Geofence>>()
        return arrayOfReminders.toList()
    }

    fun reAddAll(){
        val geofences = getAll()
        removeAll {
            geofences.forEach {
                add(it) {}
            }
        }
    }

    private fun buildGeofence(geofence: Geofence): com.google.android.gms.location.Geofence {
        return Builder()
            .setRequestId(geofence.id)
            .setLoiteringDelay(1)
            .setNotificationResponsiveness(1)
            .setCircularRegion(
                geofence.latitude,
                geofence.longitude,
                geofence.radius.toFloat()
            )
            .setTransitionTypes(geofence.transitionType)
            .setExpirationDuration(NEVER_EXPIRE)
            .build()
    }

    private fun buildGeofencingRequest(geofence: com.google.android.gms.location.Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofences(listOf(geofence))
            .build()
    }

}