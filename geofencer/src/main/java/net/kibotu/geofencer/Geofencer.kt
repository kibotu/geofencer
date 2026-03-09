package net.kibotu.geofencer

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import net.kibotu.geofencer.internal.GeofenceRepository

object Geofencer {

    private lateinit var repository: GeofenceRepository

    internal val mutableEvents = MutableSharedFlow<GeofenceEvent>(extraBufferCapacity = 64)
    internal val mutableGeofences = MutableStateFlow<List<Geofence>>(emptyList())

    val events: SharedFlow<GeofenceEvent> = mutableEvents.asSharedFlow()

    val geofences: StateFlow<List<Geofence>> = mutableGeofences.asStateFlow()

    internal fun init(context: Context) {
        repository = GeofenceRepository(context.applicationContext)
        mutableGeofences.value = repository.getAll()
    }

    private fun requireRepository(): GeofenceRepository {
        check(::repository.isInitialized) {
            "Geofencer not initialized. Add androidx.startup dependency or call Geofencer.init(context)."
        }
        return repository
    }

    suspend fun add(block: GeofenceBuilder.() -> Unit): Result<Geofence> = runCatching {
        val geofence = GeofenceBuilder().apply(block).build()
        requireRepository().add(geofence)
        mutableGeofences.value = requireRepository().getAll()
        geofence
    }

    suspend fun remove(id: String): Result<Unit> = runCatching {
        val repo = requireRepository()
        val geofence = repo.get(id) ?: return@runCatching
        repo.remove(geofence)
        mutableGeofences.value = repo.getAll()
    }

    suspend fun removeAll(): Result<Unit> = runCatching {
        requireRepository().removeAll()
        mutableGeofences.value = emptyList()
    }

    operator fun get(id: String): Geofence? = requireRepository().get(id)

    internal fun getRepository(context: Context): GeofenceRepository {
        if (!::repository.isInitialized) init(context)
        return requireRepository()
    }
}
