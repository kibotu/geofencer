package net.kibotu.geofencer.geofencer

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.kibotu.geofencer.geofencer.models.Geofence

object Geofencer {

    internal const val PREFS_NAME = "GeofenceRepository"
    internal const val REQUEST_CODE = 5999
    internal const val INTENT_EXTRAS_KEY = "geofencesId"
    internal const val EXTRA_TRANSITION_TYPE = "geofence_transition_type"
    internal const val EXTRA_TRIGGERING_LAT = "geofence_triggering_lat"
    internal const val EXTRA_TRIGGERING_LNG = "geofence_triggering_lng"
    internal const val LOCATION_UPDATE_CLASS_NAME = "location_update_worker_name"
    internal const val LOCATION_UPDATE_INTENT = "location_update_intent_string"

    private lateinit var repository: GeofenceRepository

    internal val mutableEvents = MutableSharedFlow<GeofenceEvent>(extraBufferCapacity = 64)

    val events: SharedFlow<GeofenceEvent> = mutableEvents.asSharedFlow()

    fun init(context: Context) {
        repository = GeofenceRepository(context.applicationContext)
    }

    private fun requireRepository(): GeofenceRepository {
        check(::repository.isInitialized) { "Call Geofencer.init(context) first." }
        return repository
    }

    suspend fun add(block: GeofenceSpec.() -> Unit) {
        val geofence = GeofenceSpec().apply(block).build()
        requireRepository().add(geofence)
    }

    suspend fun remove(id: String) {
        val repo = requireRepository()
        val geofence = repo.get(id) ?: return
        repo.remove(geofence)
    }

    suspend fun removeAll() {
        requireRepository().removeAll()
    }

    val all: List<Geofence>
        get() = requireRepository().getAll()

    operator fun get(id: String): Geofence? = requireRepository().get(id)

    fun parseExtras(context: Context, intent: Intent): Geofence? {
        if (!::repository.isInitialized) init(context)
        return intent.extras?.getString(INTENT_EXTRAS_KEY)?.let { get(it) }
    }

    internal fun getRepository(context: Context): GeofenceRepository {
        if (!::repository.isInitialized) init(context)
        return requireRepository()
    }
}
