package net.kibotu.geofencer.geofencer

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.Geofence.Builder
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kibotu.geofencer.R
import net.kibotu.geofencer.geofencer.models.Geofence
import net.kibotu.geofencer.geofencer.service.GeofenceBroadcastReceiver
import net.kibotu.geofencer.utils.loge
import net.kibotu.geofencer.utils.sharedPreference
import timber.log.Timber

internal class GeofenceRepository(private val context: Context) {

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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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
        val defaultLoitering = context.resources.getInteger(R.integer.loitering_delay)
        val defaultResponsiveness = context.resources.getInteger(R.integer.notification_responsiveness)

        return Builder()
            .setRequestId(geofence.id)
            .setLoiteringDelay(
                if (geofence.loiteringDelayMillis >= 0) geofence.loiteringDelayMillis.toInt()
                else defaultLoitering
            )
            .setNotificationResponsiveness(
                if (geofence.responsivenessMillis >= 0) geofence.responsivenessMillis.toInt()
                else defaultResponsiveness
            )
            .setCircularRegion(
                geofence.latitude,
                geofence.longitude,
                geofence.radius.toFloat()
            )
            .setTransitionTypes(geofence.transitions)
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
