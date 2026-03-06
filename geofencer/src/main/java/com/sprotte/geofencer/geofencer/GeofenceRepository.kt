package com.sprotte.geofencer.geofencer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Geofence.Builder
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.sprotte.geofencer.R
import com.sprotte.geofencer.geofencer.models.Geofence
import com.sprotte.geofencer.geofencer.service.GeofenceBroadcastReceiver
import com.sprotte.geofencer.utils.getRes
import com.sprotte.geofencer.utils.loge
import com.sprotte.geofencer.utils.sharedPreference
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

class GeofenceRepository(private val context: Context) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent
        get() {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(
                context,
                Geofencer.REQUEST_CODE,
                intent,
                flags
            )
        }

    var geofenceString by context.sharedPreference(Geofencer.PREFS_NAME, "")

    @SuppressLint("MissingPermission")
    suspend fun add(geofence: Geofence) {
        val androidGeofence = buildGeofence(geofence)
        geofencingClient
            .addGeofences(buildGeofencingRequest(androidGeofence), geofencePendingIntent)
            .await()
        saveAll(getAll() + geofence)
    }

    suspend fun remove(geofence: Geofence) {
        geofencingClient.removeGeofences(listOf(geofence.id)).await()
        saveAll(getAll() - geofence)
    }

    suspend fun removeAll() {
        try {
            geofencingClient.removeGeofences(geofencePendingIntent).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove geofences from client")
        }
        geofenceString = ""
    }

    fun get(requestId: String?) = getAll().firstOrNull { it.id == requestId }

    fun getAll(): List<Geofence> {
        if (geofenceString.isBlank()) return emptyList()
        return try {
            Json.decodeFromString<List<Geofence>>(geofenceString)
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode geofences")
            emptyList()
        }
    }

    private fun saveAll(geofences: List<Geofence>) {
        geofenceString = Json.encodeToString(geofences)
    }

    @SuppressLint("MissingPermission")
    suspend fun reAddAll() {
        val geofences = getAll()
        removeAll()
        geofences.forEach { geofence ->
            try {
                add(geofence)
            } catch (e: Exception) {
                loge("Failed to re-add geofence ${geofence.id}: ${e.message}")
            }
        }
    }

    private fun buildGeofence(geofence: Geofence): com.google.android.gms.location.Geofence {
        return Builder()
            .setRequestId(geofence.id)
            .setLoiteringDelay(context.resources.getInteger(R.integer.loitering_delay))
            .setNotificationResponsiveness(context.resources.getInteger(R.integer.notification_responsiveness))
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
