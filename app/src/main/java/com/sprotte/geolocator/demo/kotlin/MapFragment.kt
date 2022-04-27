package com.sprotte.geolocator.demo.kotlin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import com.sprotte.geolocator.demo.R
import com.sprotte.geolocator.demo.databinding.FragmentMapBinding
import com.sprotte.geolocator.demo.misc.*
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.geofencer.models.Geofence
import com.sprotte.geolocator.tracking.LocationTracker
import timber.log.Timber
import kotlin.math.roundToInt


class MapFragment : Fragment(), GoogleMap.OnMarkerClickListener {

    private var binding: FragmentMapBinding? = null

    private var map: GoogleMap? = null

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

                Timber.v("OnSharedPreferenceChange 1 $locationResult")
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = FragmentMapBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.setup()

        requireContext().getSharedPrefs().registerOnSharedPreferenceChangeListener(preferenceChangedListener)
    }

    @SuppressLint("MissingPermission")
    private fun FragmentMapBinding.setup() {

        newReminder.isGone = true
        currentLocation.isGone = true

        currentLocation.setOnClickListener {

            val locationManager = requireContext().getSystemService<LocationManager>() ?: return@setOnClickListener
            val bestProvider = locationManager.getBestProvider(Criteria(), false) ?: return@setOnClickListener

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
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            this@MapFragment.map = map
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
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                    showGeofences()
                }
            }
            map.onMapReady()
        }
    }

    override fun onDestroyView() {
        map = null
        binding = null
        super.onDestroyView()
    }

    private fun GoogleMap.onMapReady() {
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.isMapToolbarEnabled = false
        setOnMarkerClickListener(this@MapFragment)
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(geofence: Geofence) {
        requireActivity().requestLocationPermission {
            if (it.granted) {
                Geofencer(requireContext())
                    .addGeofence(geofence, GeofenceIntentService::class.java) {
                        binding?.container?.isGone = true
                        showGeofences()
                    }
            }
        }
    }

    private fun showGeofenceUpdate() {
        map?.clear()
        showGeofenceInMap(requireContext(), map!!, geofence)
    }

    private fun showGeofences() {
        map?.run {
            clear()
            for (geofence in Geofencer(requireContext()).getAll()) {
                showGeofenceInMap(requireContext(), this, geofence)
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val geofence = Geofencer(requireContext()).get(marker.tag as String)
        if (geofence != null) {
            binding?.showGeofenceRemoveAlert(geofence)
        }
        return true
    }

    private fun FragmentMapBinding.showGeofenceRemoveAlert(geofence: Geofence) {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
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

    private fun FragmentMapBinding.removeGeofence(geofence: Geofence) {
        Geofencer(requireContext()).removeGeofence(geofence.id) {
            showGeofences()
            Snackbar.make(
                main,
                R.string.reminder_removed_success, Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun FragmentMapBinding.showConfigureLocationStep() {
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

    private fun FragmentMapBinding.showConfigureRadiusStep() {
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

    private fun FragmentMapBinding.showConfigureMessageStep() {
        marker.isGone = true
        instructionTitle.isVisible = true
        instructionSubtitle.isGone = true
        radiusBar.isGone = true
        radiusDescription.isGone = true
        message.isVisible = true
        instructionTitle.text = getString(R.string.instruction_message_description)
        next.setOnClickListener {
            hideKeyboard(requireContext(), message)
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
            binding?.updateRadiusWithProgress(progress)
            showGeofenceUpdate()
        }
    }

    private fun FragmentMapBinding.updateRadiusWithProgress(progress: Int) {
        val radius = getRadius(progress)
        geofence.radius = radius
        radiusDescription.text =
            getString(R.string.radius_description, radius.roundToInt().toString())
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        val locationManager = requireContext().getSystemService<LocationManager>() ?: return null
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                bestLocation = l
            }
        }
        return bestLocation
    }
}

