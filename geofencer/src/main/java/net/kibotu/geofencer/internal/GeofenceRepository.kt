package net.kibotu.geofencer.internal

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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kibotu.geofencer.Geofence
import timber.log.Timber
import kotlin.time.Duration

internal class GeofenceRepository(private val context: Context) {

    private val mutex = Mutex()
    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val prefs = context.getSharedPreferences(Prefs.GEOFENCE_PREFS, Context.MODE_PRIVATE)

    private val geofencePendingIntent: PendingIntent
        get() {
            val intent = Intent(context, GeofenceReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, Extras.REQUEST_CODE, intent, flags)
        }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun add(geofence: Geofence): Unit = mutex.withLock {
        val androidGeofence = buildGeofence(geofence)
        geofencingClient
            .addGeofences(buildGeofencingRequest(androidGeofence), geofencePendingIntent)
            .await()
        saveAll(getAll() + geofence)
    }

    suspend fun remove(geofence: Geofence): Unit = mutex.withLock {
        geofencingClient.removeGeofences(listOf(geofence.id)).await()
        saveAll(getAll() - geofence)
    }

    suspend fun removeAll(): Unit = mutex.withLock {
        try {
            geofencingClient.removeGeofences(geofencePendingIntent).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove geofences from client")
        }
        saveAll(emptyList())
    }

    fun get(requestId: String?): Geofence? = getAll().firstOrNull { it.id == requestId }

    fun getAll(): List<Geofence> {
        val json = prefs.getString(Prefs.GEOFENCE_KEY, null)
        if (json.isNullOrBlank()) return emptyList()
        return try {
            Json.decodeFromString<List<Geofence>>(json)
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode geofences")
            emptyList()
        }
    }

    private fun saveAll(geofences: List<Geofence>) {
        prefs.edit()
            .putString(Prefs.GEOFENCE_KEY, Json.encodeToString(geofences))
            .apply()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun reAddAll() {
        val saved = getAll()
        if (saved.isEmpty()) return

        try {
            geofencingClient.removeGeofences(geofencePendingIntent).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove geofences during re-registration")
        }

        for (geofence in saved) {
            try {
                val androidGeofence = buildGeofence(geofence)
                geofencingClient
                    .addGeofences(buildGeofencingRequest(androidGeofence), geofencePendingIntent)
                    .await()
            } catch (e: Exception) {
                Timber.e(e, "Failed to re-register geofence ${geofence.id}")
            }
        }
    }

    private fun buildGeofence(geofence: Geofence): com.google.android.gms.location.Geofence {
        return Builder()
            .setRequestId(geofence.id)
            .setLoiteringDelay(
                geofence.loiteringDelay.takeIf { it > Duration.ZERO }
                    ?.inWholeMilliseconds?.toInt()
                    ?: DEFAULT_LOITERING_DELAY_MS
            )
            .setNotificationResponsiveness(
                geofence.responsiveness.takeIf { it > Duration.ZERO }
                    ?.inWholeMilliseconds?.toInt()
                    ?: DEFAULT_RESPONSIVENESS_MS
            )
            .setCircularRegion(
                geofence.latitude,
                geofence.longitude,
                geofence.radius.toFloat()
            )
            .setTransitionTypes(geofence.transitionBitmask())
            .setExpirationDuration(
                if (geofence.expiration.isInfinite()) NEVER_EXPIRE
                else geofence.expiration.inWholeMilliseconds
            )
            .build()
    }

    private fun buildGeofencingRequest(
        geofence: com.google.android.gms.location.Geofence,
    ): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofences(listOf(geofence))
            .build()
    }

    companion object {
        private const val DEFAULT_LOITERING_DELAY_MS = 30_000
        private const val DEFAULT_RESPONSIVENESS_MS = 300_000
    }
}
