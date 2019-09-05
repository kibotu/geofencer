package com.sprotte.geolocator.demo

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.exozet.android.core.base.BaseFragment
import com.github.florent37.application.provider.application
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.geofencer.models.Geofence
import com.sprotte.geolocator.tracking.LocationTracker
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_map.*
import net.kibotu.logger.Logger
import kotlin.math.roundToInt


class MapFragment : BaseFragment(), GoogleMap.OnMarkerClickListener {

    override val layout = R.layout.fragment_map

    private var map: GoogleMap? = null

    private lateinit var locationManager: LocationManager

    private var geofence = Geofence()

    private val preferenceChangedListener =
        SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
            val value = preferences.getString(key, "invalid")
            Logger.v("OnSharedPreferenceChange $key $value")
        }


    override fun subscribeUi() {
        super.subscribeUi()

        this.context?.getSharedPrefs()?.registerOnSharedPreferenceChangeListener(preferenceChangedListener)

        newReminder.visibility = View.GONE
        currentLocation.visibility = View.GONE

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
            requestLocationPermission {

                LocationTracker
                    .removeLocationUpdates(requireContext())
                LocationTracker
                    .requestLocationUpdates(requireContext(), AppTrackerService::class.java)


                map.isMyLocationEnabled = it.granted

                if (it.granted) {
                    newReminder.visibility = View.VISIBLE
                    currentLocation.visibility = View.VISIBLE
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

    fun requestLocationPermission(block: (permission: Permission) -> Unit) = RxPermissions(this)
        .requestEachCombined(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        .subscribe({
            block(it)
        }, {
            Log.v("sasd", "location permission $it")
        })
        .addTo(subscription)

    @SuppressLint("MissingPermission")
    private fun addGeofence(geofence: Geofence) {
        requestLocationPermission {
            if (it.granted) {
                Geofencer(requireContext())
                    .addGeofence(geofence, AppGeofenceService::class.java) {
                        container.visibility = View.GONE
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
            Snackbar.make(main, R.string.reminder_removed_success, Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun showConfigureLocationStep() {
        container.visibility = View.VISIBLE
        marker.visibility = View.VISIBLE
        instructionTitle.visibility = View.VISIBLE
        instructionSubtitle.visibility = View.VISIBLE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.GONE
        instructionTitle.text = getString(R.string.instruction_where_description)
        next.setOnClickListener {
            geofence.latitude = map?.cameraPosition?.target?.latitude ?: 0.0
            geofence.longitude = map?.cameraPosition?.target?.longitude ?: 0.0
            showConfigureRadiusStep()
        }
        showGeofenceUpdate()
    }

    private fun showConfigureRadiusStep() {
        marker.visibility = View.GONE
        instructionTitle.visibility = View.VISIBLE
        instructionSubtitle.visibility = View.GONE
        radiusBar.visibility = View.VISIBLE
        radiusDescription.visibility = View.VISIBLE
        message.visibility = View.GONE
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
        marker.visibility = View.GONE
        instructionTitle.visibility = View.VISIBLE
        instructionSubtitle.visibility = View.GONE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.VISIBLE
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

