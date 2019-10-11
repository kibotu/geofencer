package com.sprotte.geolocator.demo.kotlin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.widget.SeekBar
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.exozet.android.core.base.BaseFragment
import com.exozet.android.core.gson.fromJson
import com.exozet.android.core.gson.toJson
import com.exozet.android.core.storage.sharedPreference
import com.github.florent37.application.provider.application
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import com.sprotte.geolocator.demo.R
import com.sprotte.geolocator.demo.misc.*
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.geofencer.models.Geofence
import com.sprotte.geolocator.tracking.LocationTracker
import kotlinx.android.synthetic.main.fragment_map.*
import net.kibotu.logger.Logger
import kotlin.math.roundToInt


class MapFragment : BaseFragment(), GoogleMap.OnMarkerClickListener {

    override val layout = R.layout.fragment_map

    private var map: GoogleMap? = null

    private lateinit var locationManager: LocationManager

    private var geofence = Geofence()

    private val preferenceChangedListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->

            // key has been updated
            if (key == LocationTrackerService.PREFERENCE_LOCATION) {

                // retrieve location from preferences
                val locationResult = sharedPreferences.getString(key, null)

                // retrieve location data:
                // val lastLocation = locationResult.lastLocation
                // val locations = locationResult.locations
                // locations.forEach {
                //     it.latitude
                //     it.longitude
                //     it.altitude
                //     it.speed
                //     it.bearing
                // }

                // lastLocation.latitude
                // lastLocation.longitude
                // lastLocation.altitude
                // lastLocation.speed
                // lastLocation.bearing

                Logger.v("OnSharedPreferenceChange 1 $locationResult")
            }
        }

    override fun subscribeUi() {
        super.subscribeUi()

        this.context?.getSharedPrefs()?.registerOnSharedPreferenceChangeListener(preferenceChangedListener)

        newReminder.isGone = true
        currentLocation.isGone = true

        locationManager = application?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        currentLocation.setOnClickListener {
            val bestProvider = locationManager.getBestProvider(Criteria(), false)
            @SuppressLint("MissingPermission")
            val location = locationManager.getLastKnownLocation(bestProvider)
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }

        newReminder.setOnClickListener {
            showConfigureLocationStep()
        }

        mapFragment.getMapAsync { map ->
            this.map = map
            requireActivity().requestLocationPermission {

                LocationTracker.removeLocationUpdates(requireContext())
                LocationTracker.requestLocationUpdates(requireContext(), LocationTrackerService::class.java)


                map.isMyLocationEnabled = it.granted

                if (it.granted) {
                    newReminder.isVisible = true
                    currentLocation.isVisible = true
                    @SuppressLint("MissingPermission")
                    val location = getLastKnownLocation()
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                    showGeofences()
                }
            }
            onMapReady(map)
        }
    }

    override fun unsubscribeUi() {
        super.unsubscribeUi()
        map = null
    }

    fun onMapReady(googleMap: GoogleMap) {

        with(googleMap) {
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            setOnMarkerClickListener(this@MapFragment)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(geofence: Geofence) {
        requireActivity().requestLocationPermission {
            if (it.granted) {
                Geofencer(requireContext())
                    .addGeofence(geofence, GeofenceIntentService::class.java) {
                        container.isGone = true
                        showGeofences()
                    }
            }
        }
    }

    private fun showGeofenceUpdate() {
        map?.clear()
        showGeofenceInMap(context!!, map!!, geofence)
    }

    private fun showGeofences() {
        map?.run {
            clear()
            for (geofence in Geofencer(requireContext()).getAll()) {
                showGeofenceInMap(context!!, this, geofence)
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val geofence = Geofencer(requireContext()).get(marker.tag as String)
        if (geofence != null) {
            showGeofenceRemoveAlert(geofence)
        }
        return true
    }

    private fun showGeofenceRemoveAlert(geofence: Geofence) {
        val alertDialog = AlertDialog.Builder(context!!).create()
        alertDialog.run {
            setMessage(getString(R.string.reminder_removal_alert))
            setButton(
                AlertDialog.BUTTON_POSITIVE,
                getString(R.string.reminder_removal_alert_positive)
            ) { dialog, _ ->
                removeGeofence(geofence)
                dialog.dismiss()
            }
            setButton(
                AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.reminder_removal_alert_negative)
            ) { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun removeGeofence(geofence: Geofence) {
        Geofencer(requireContext()).removeGeofence(geofence.id) {
            showGeofences()
            Snackbar.make(
                main,
                R.string.reminder_removed_success, Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun showConfigureLocationStep() {
        container.isVisible = true
        marker.isVisible = true
        instructionTitle.isVisible = true
        instructionSubtitle.isVisible = true
        radiusBar.isGone = true
        radiusDescription.isGone = true
        message.isGone = true
        instructionTitle.text = getString(R.string.instruction_where_description)
        next.setOnClickListener {
            geofence.latitude = map?.cameraPosition?.target?.latitude ?: 0.0
            geofence.longitude = map?.cameraPosition?.target?.longitude ?: 0.0
            showConfigureRadiusStep()
        }
        showGeofenceUpdate()
    }

    private fun showConfigureRadiusStep() {
        marker.isGone = true
        instructionTitle.isVisible = true
        instructionSubtitle.isGone = true
        radiusBar.isVisible = true
        radiusDescription.isVisible = true
        message.isGone = true
        instructionTitle.text = getString(R.string.instruction_radius_description)
        next.setOnClickListener {
            showConfigureMessageStep()
        }
        radiusBar.setOnSeekBarChangeListener(radiusBarChangeListener)
        updateRadiusWithProgress(radiusBar.progress)
        map?.animateCamera(CameraUpdateFactory.zoomTo(15f))
        showGeofenceUpdate()
    }

    private fun showConfigureMessageStep() {
        marker.isGone = true
        instructionTitle.isVisible = true
        instructionSubtitle.isGone = true
        radiusBar.isGone = true
        radiusDescription.isGone = true
        message.isVisible = true
        instructionTitle.text = getString(R.string.instruction_message_description)
        next.setOnClickListener {
            hideKeyboard(context!!, message)
            geofence.message = message.text.toString()

            if (geofence.message.isNullOrEmpty()) {
                message.error = getString(R.string.error_required)
            } else {
                addGeofence(geofence)
            }
        }
        message.requestFocusWithKeyboard()
        showGeofenceUpdate()
    }

    private fun getRadius(progress: Int) = 100 + (2 * progress.toDouble() + 1) * 100

    private val radiusBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            updateRadiusWithProgress(progress)
            showGeofenceUpdate()
        }
    }

    private fun updateRadiusWithProgress(progress: Int) {
        val radius = getRadius(progress)
        geofence.radius = radius
        radiusDescription.text =
            getString(R.string.radius_description, radius.roundToInt().toString())
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.getAccuracy() < bestLocation!!.getAccuracy()) {
                bestLocation = l
            }
        }
        return bestLocation
    }
}

