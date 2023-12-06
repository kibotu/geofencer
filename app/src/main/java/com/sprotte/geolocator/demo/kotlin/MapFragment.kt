package com.sprotte.geolocator.demo.kotlin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
import com.sprotte.geolocator.utils.showTwoButtonDialog
import com.tbruyelle.rxpermissions2.Permission
import timber.log.Timber
import kotlin.math.roundToInt


class MapFragment : Fragment(), GoogleMap.OnMarkerClickListener {

    private var binding: FragmentMapBinding? = null

    private var map: GoogleMap? = null

    private var geofence = Geofence()

    @SuppressLint("MissingPermission")
    val permissionGivenLambda: (Permission) -> Unit = {
        map?.isMyLocationEnabled = it.granted
        if (it.granted) {
            LocationTracker.removeLocationUpdates(requireContext())
            LocationTracker.requestLocationUpdates(requireContext(), LocationTrackerWorker::class.java)
            binding?.run {
                newReminder.isVisible = true
                currentLocation.isVisible = true
            }
        }

        val location = getLastKnownLocation()
        if (location != null) {
            val latLng = LatLng(location.latitude, location.longitude)
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }
        showGeofences()
    }

    val permissionRationaleLambda: (Permission) -> Unit = {
        Timber.d(
            "shouldShowRequestPermissionRationale :" +
                    "${it.shouldShowRequestPermissionRationale} \n for ${it.name}"
        )
        (activity as FragmentActivity).showTwoButtonDialog(getString(R.string.dialog_rationale_coarse_location)) {
            if (it) {
                // ask for permission again
                Timber.d("ask for permission again")
                requestLocationPermissionLambda()
            } else {
                // permission for denied
                Timber.d("permission was denied")
            }
        }
    }

    @SuppressLint("MissingPermission")
    val requestLocationPermissionLambda: () -> Unit = {
        val corePermissionFlow: (Permission) -> Unit = {

            map?.isMyLocationEnabled = it.granted

            when {
                it.granted -> permissionGivenLambda(it)
                it.shouldShowRequestPermissionRationale -> permissionRationaleLambda(it)
                else -> {
                    // goto settings
                    Timber.d("goto settings for ${it.name}")
                }
            }
        }
        requireActivity().requestLocationPermission {
            corePermissionFlow(it)
        }
    }

    private var hasShownPNRational = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // PN permission is granted, now ask for location permission
                requestLocationPermissionLambda()
                return@registerForActivityResult
            }
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // PN is not granted
                if (!hasShownPNRational) {
                    // PN rationale dialog was not shown
                    hasShownPNRational = true
                    askForPNRationale()
                } else {
                    // PN is not granted even after rationale dialog, now asking for location permission
                    requestLocationPermissionLambda()
                }
                return@registerForActivityResult
            }
        }

    val checkNotificationPermission: ()-> Unit = {
        // check for notification only on new permission system introduced in Android Api 33
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            requestLocationPermissionLambda()
        }
    }

    private val askForPNRationale: ()-> Unit = {
        requireActivity().showTwoButtonDialog(getString(R.string.dialog_rationale_permission)) {
            if (it) {
                // ask again after rationale user action is positive
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val preferenceChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->

        Timber.v("OnSharedPreferenceChange key=$key")

        // key has been updated
        if (key == LocationTrackerWorker.USER_LOCATION_KEY) {

            // retrieve location from preferences
            val locationResult = sharedPreferences.getString(key, null)
            Timber.v("OnSharedPreferenceChange 1 $locationResult")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentMapBinding.inflate(
        inflater,
        container,
        false
    ).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.setup()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(preferenceChangedListener)
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
            /**
             * start asking all permissions
             * 1. Notification (only if running on Android 33 and above)
             * 2. FINE AND COARSE Location
             * 3. Background Location (only if running on Android 29 and above)
             * */
            checkNotificationPermission()
            map.onMapReady()
        }
    }

    override fun onDestroyView() {
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(preferenceChangedListener)
        map = null
        binding = null
        super.onDestroyView()
    }

    private fun GoogleMap.onMapReady() {
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.isMapToolbarEnabled = false
        uiSettings.isZoomControlsEnabled = true
        setOnMarkerClickListener(this@MapFragment)
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(geofence: Geofence) {
        requireActivity().requestLocationPermission {
            if (it.granted) {
                Geofencer(requireContext())
                    .addGeofenceWorker(geofence, NotificationWorker::class.java) {
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

