package com.sprotte.geolocator.geofencer

import android.content.Context
import android.content.Intent
import com.sprotte.geolocator.geofencer.models.GeoFenceUpdateModule
import com.sprotte.geolocator.geofencer.models.Geofence

class Geofencer(context: Context) {

    companion object {

        fun parseExtras(context: Context, intent: Intent): Geofence? {
            val id = intent.extras?.getString(INTENT_EXTRAS_KEY)
            if (id != null) {
                return Geofencer(context).get(id)
            }

            return null
        }

        const val PREFS_NAME = "GeofenceRepository"
        const val REQUEST_CODE = 5999
        const val INTENT_EXTRAS_KEY = "geofencesId"

        const val LOCATION_UPDATE_CLASS_NAME = "location_update_worker_name"
        const val LOCATION_UPDATE_INTENT = "location_update_intent_string"
    }

    var repository = GeofenceRepository(context)

    fun <T : GeoFenceUpdateModule> addGeofenceWorker(geofence: Geofence, intent: Class<T>, success: (() -> Unit)? = null) {
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