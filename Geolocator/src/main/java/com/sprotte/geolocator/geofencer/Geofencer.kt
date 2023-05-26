package com.sprotte.geolocator.geofencer

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.JobIntentService
import com.sprotte.geolocator.geofencer.models.CoreWorkerModule
import com.sprotte.geolocator.geofencer.models.Geofence

class Geofencer(context: Context) {

    companion object {

        fun parseExtras(context: Context, intent: Intent): Geofence? {
            val id = intent.extras?.getString(Geofencer.INTENT_EXTRAS_KEY)
            if (id != null) {
                return Geofencer(context).get(id)
            }

            return null
        }

        const val PREFS_NAME = "GeofenceRepository"
        const val REQUEST_CODE = 5999
        const val INTENT_EXTRAS_KEY = "geofencesId"
    }

    var repository = GeofenceRepository(context)

    fun <T : CoreWorkerModule> addGeofenceWorker(geofence: Geofence, intent: Class<T>, success: (() -> Unit)? = null) {
        geofence.intentClassName = intent.canonicalName ?: ""
        repository.add(geofence) {
            success?.invoke()
        }
    }

    fun removeGeofence(id: String, success: () -> Unit) {
        val geofence = repository.get(id) ?: return
        repository.remove(geofence, success)
    }

    fun removeAll(success: () -> Unit) {
        repository.removeAll {
            success()
        }
    }

    fun get(id: String): Geofence? {
        return repository.get(id)
    }

    fun getAll(): List<Geofence> {
        return repository.getAll()
    }
}