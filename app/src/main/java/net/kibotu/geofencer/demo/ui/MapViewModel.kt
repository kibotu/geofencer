package net.kibotu.geofencer.demo.ui

import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import net.kibotu.geofencer.demo.kotlin.BreachMarker
import net.kibotu.geofencer.demo.kotlin.BreachMarkerRepository
import net.kibotu.geofencer.demo.kotlin.LocationLogAction
import net.kibotu.geofencer.demo.kotlin.LogEntry
import net.kibotu.geofencer.demo.kotlin.MapStyle
import net.kibotu.geofencer.demo.kotlin.NotificationAction
import net.kibotu.geofencer.geofencer.Geofencer
import net.kibotu.geofencer.geofencer.models.Geofence
import net.kibotu.geofencer.geofencer.models.Geofence.Transition
import net.kibotu.geofencer.tracking.LocationTracker
import timber.log.Timber

sealed interface WizardState {
    data object Hidden : WizardState
    data class PickLocation(val latLng: LatLng? = null) : WizardState
    data class PickRadius(val latLng: LatLng, val radius: Double = 500.0) : WizardState
    data class PickMessage(val latLng: LatLng, val radius: Double, val message: String = "") : WizardState
}

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("geofencer_prefs", Context.MODE_PRIVATE)

    var currentMapStyle by mutableStateOf(loadSavedStyle())
        private set

    var wizardState by mutableStateOf<WizardState>(WizardState.Hidden)
        private set

    private val _logEntries = mutableStateListOf<LogEntry>()
    val logEntries: List<LogEntry> = _logEntries

    var geofences by mutableStateOf(Geofencer.all)
        private set

    var breachMarkers by mutableStateOf(BreachMarkerRepository.getAll(application))
        private set

    var permissionsGranted by mutableStateOf(false)
        private set

    var markerToRemove by mutableStateOf<Geofence?>(null)
        private set

    val wizardPreview: Geofence?
        get() = when (val state = wizardState) {
            is WizardState.Hidden -> null
            is WizardState.PickLocation -> state.latLng?.let {
                Geofence(latitude = it.latitude, longitude = it.longitude, radius = 500.0)
            }
            is WizardState.PickRadius -> Geofence(
                latitude = state.latLng.latitude,
                longitude = state.latLng.longitude,
                radius = state.radius,
            )
            is WizardState.PickMessage -> Geofence(
                latitude = state.latLng.latitude,
                longitude = state.latLng.longitude,
                radius = state.radius,
            )
        }

    init {
        collectGeofenceEvents()
        collectLocationUpdates()
    }

    private fun collectGeofenceEvents() {
        viewModelScope.launch {
            Geofencer.events.collect { event ->
                _logEntries.add(0, LogEntry.Fence(event))
                trimLog()
                if (event.hasTriggeringLocation) {
                    val marker = BreachMarker(
                        latitude = event.triggeringLatitude,
                        longitude = event.triggeringLongitude,
                        geofenceId = event.geofence.id,
                        geofenceLabel = event.geofence.label,
                        transition = event.transition.name,
                    )
                    BreachMarkerRepository.add(getApplication(), marker)
                }
                refreshMapData()
            }
        }
    }

    private fun collectLocationUpdates() {
        viewModelScope.launch {
            LocationTracker.locations.collect { result ->
                _logEntries.add(0, LogEntry.Location(result))
                trimLog()
            }
        }
    }

    private fun trimLog() {
        while (_logEntries.size > MAX_LOG_ENTRIES) {
            _logEntries.removeAt(_logEntries.lastIndex)
        }
    }

    fun clearLog() {
        _logEntries.clear()
    }

    fun onPermissionsGranted() {
        permissionsGranted = true
        val context = getApplication<Application>()
        LocationTracker.stop(context)
        LocationTracker.start(context) {
            action<LocationLogAction>()
        }
        refreshMapData()
    }

    fun getLastKnownLatLng(): LatLng? {
        val locationManager = getApplication<Application>().getSystemService<LocationManager>() ?: return null
        return try {
            val providers = locationManager.getProviders(true)
            var bestLocation: android.location.Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            bestLocation?.let { LatLng(it.latitude, it.longitude) }
        } catch (_: SecurityException) {
            null
        }
    }

    fun setMapStyle(style: MapStyle) {
        currentMapStyle = style
        prefs.edit().putString("map_style", style.name).apply()
    }

    private fun loadSavedStyle(): MapStyle {
        val name = prefs.getString("map_style", null) ?: MapStyle.POKEMON_GO.name
        return MapStyle.fromName(name)
    }

    fun startWizard(cameraTarget: LatLng) {
        wizardState = WizardState.PickLocation(cameraTarget)
    }

    fun updateWizardLocation(latLng: LatLng) {
        val state = wizardState
        if (state is WizardState.PickLocation) {
            wizardState = state.copy(latLng = latLng)
        }
    }

    fun confirmLocation(latLng: LatLng) {
        wizardState = WizardState.PickRadius(latLng = latLng)
    }

    fun updateRadius(radius: Double) {
        val state = wizardState
        if (state is WizardState.PickRadius) {
            wizardState = state.copy(radius = radius)
        }
    }

    fun confirmRadius() {
        val state = wizardState
        if (state is WizardState.PickRadius) {
            wizardState = WizardState.PickMessage(latLng = state.latLng, radius = state.radius)
        }
    }

    fun updateMessage(message: String) {
        val state = wizardState
        if (state is WizardState.PickMessage) {
            wizardState = state.copy(message = message)
        }
    }

    fun submitGeofence() {
        val state = wizardState as? WizardState.PickMessage ?: return
        if (state.message.isBlank()) return
        viewModelScope.launch {
            try {
                Geofencer.add {
                    latitude = state.latLng.latitude
                    longitude = state.latLng.longitude
                    radius = state.radius
                    message = state.message
                    transitions = Transition.Enter + Transition.Exit
                    action<NotificationAction>()
                }
                wizardState = WizardState.Hidden
                refreshMapData()
            } catch (e: Exception) {
                Timber.e(e, "Failed to add geofence")
            }
        }
    }

    fun cancelWizard() {
        wizardState = WizardState.Hidden
    }

    fun onMarkerClicked(geofenceId: String) {
        markerToRemove = Geofencer[geofenceId]
    }

    fun dismissRemoveDialog() {
        markerToRemove = null
    }

    fun confirmRemoveGeofence() {
        val geofence = markerToRemove ?: return
        markerToRemove = null
        viewModelScope.launch {
            try {
                Geofencer.remove(geofence.id)
                refreshMapData()
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove geofence")
            }
        }
    }

    private fun refreshMapData() {
        geofences = Geofencer.all
        breachMarkers = BreachMarkerRepository.getAll(getApplication())
    }

    companion object {
        private const val MAX_LOG_ENTRIES = 200
    }
}
