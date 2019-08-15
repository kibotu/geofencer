package com.sprotte.geofencer

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.JobIntentService
import com.sprotte.geofencer.models.Geofence

class Geofencer(context: Context) {

    companion object {
        const val PREFS_NAME = "GeofenceRepository"
        const val REQUEST_CODE = 5999
        const val INTENT_EXTRAS_KEY = "geofencesId"
    }

    var repository = GeofenceRepository(context)

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    inline fun <reified T : JobIntentService> addGeofence(
        geofence: Geofence,
        intent: Class<T>,
        crossinline success: () -> Unit
    ) {
        geofence.intentClassName = intent.canonicalName!!
        repository.add(geofence){
            success()
        }
    }

    fun removeGeofence(
        id: String,
        success: () -> Unit
    ) {
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