package net.kibotu.geofencer.demo.kotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import net.kibotu.geofencer.demo.R
import net.kibotu.geofencer.demo.databinding.FragmentMapBinding
import net.kibotu.geofencer.demo.misc.hideKeyboard
import net.kibotu.geofencer.demo.misc.requestFocusWithKeyboard
import net.kibotu.geofencer.demo.misc.showGeofenceInMap
import net.kibotu.geofencer.demo.misc.showTwoButtonDialog
import net.kibotu.geofencer.geofencer.Geofencer
import net.kibotu.geofencer.geofencer.models.Geofence
import net.kibotu.geofencer.tracking.LocationTracker
import timber.log.Timber
import kotlin.math.roundToInt

class MapFragment : Fragment(), GoogleMap.OnMarkerClickListener {

    private var binding: FragmentMapBinding? = null
    private var map: GoogleMap? = null

    private var pendingLatitude: Double = 0.0
    private var pendingLongitude: Double = 0.0
    private var pendingRadius: Double = 0.0
    private var pendingMessage: String = ""

    private val sharedPreferences: SharedPreferences? by lazy {
        context?.let {
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(it)
        }
    }

    // region Permission launchers

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                requestForegroundLocation()
                return@registerForActivityResult
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                requireActivity().showTwoButtonDialog(getString(R.string.dialog_rationale_permission)) { accepted ->
                    if (accepted) launchNotificationPermission()
                }
            } else {
                requestForegroundLocation()
            }
        }

    private fun launchNotificationPermission() {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private val foregroundLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fineGranted || coarseGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundLocation()
                } else {
                    onAllPermissionsGranted()
                }
            } else {
                val shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                if (shouldShowRationale) {
                    requireActivity().showTwoButtonDialog(getString(R.string.dialog_rationale_coarse_location)) {
                        if (it) requestForegroundLocation()
                    }
                } else {
                    Timber.d("Location permission permanently denied")
                }
            }
        }

    private val backgroundLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onAllPermissionsGranted()
            } else {
                Timber.d("Background location permission denied")
                onAllPermissionsGranted()
            }
        }

    // endregion

    private fun startPermissionFlow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            requestForegroundLocation()
        }
    }

    private fun requestForegroundLocation() {
        foregroundLocationLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    private fun onAllPermissionsGranted() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation) {
            map?.isMyLocationEnabled = true
            LocationTracker.stop(requireContext())
            LocationTracker.start(requireContext()) {
                action<LocationLogAction>()
            }
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

    private val preferenceChangedListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            Timber.v("OnSharedPreferenceChange key=$key")
            if (key == LocationLogAction.USER_LOCATION_KEY) {
                val locationResult = sharedPreferences.getString(key, null)
                Timber.v("OnSharedPreferenceChange: $locationResult")
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
        binding?.applySystemInsets()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(preferenceChangedListener)
    }

    private fun FragmentMapBinding.applySystemInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(fabContainer) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(bottom = insets.bottom, right = insets.right)
            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(containerSettings) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime()
            )
            v.updatePadding(bottom = insets.bottom, left = insets.left, right = insets.right)
            windowInsets
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun FragmentMapBinding.setup() {
        newReminder.isGone = true
        currentLocation.isGone = true

        currentLocation.setOnClickListener {
            val locationManager = requireContext().getSystemService<LocationManager>() ?: return@setOnClickListener
            val location = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
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
            startPermissionFlow()
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

    private fun addGeofence() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Geofencer.add {
                    latitude = pendingLatitude
                    longitude = pendingLongitude
                    radius = pendingRadius
                    message = pendingMessage
                    action<NotificationAction>()
                }
                binding?.container?.isGone = true
                showGeofences()
            } catch (e: Exception) {
                Timber.e(e, "Failed to add geofence")
            }
        }
    }

    private fun showGeofenceUpdate() {
        map?.clear()
        val preview = Geofence(
            latitude = pendingLatitude,
            longitude = pendingLongitude,
            radius = pendingRadius,
        )
        showGeofenceInMap(requireContext(), map!!, preview)
    }

    private fun showGeofences() {
        map?.run {
            clear()
            for (geofence in Geofencer.all) {
                showGeofenceInMap(requireContext(), this, geofence)
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val geofence = Geofencer[marker.tag as String]
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
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Geofencer.remove(geofence.id)
                showGeofences()
                Snackbar.make(
                    main,
                    R.string.reminder_removed_success, Snackbar.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove geofence")
            }
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
            pendingLatitude = map?.cameraPosition?.target?.latitude ?: 0.0
            pendingLongitude = map?.cameraPosition?.target?.longitude ?: 0.0
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
            pendingMessage = message.text.toString()
            if (pendingMessage.isEmpty()) {
                message.error = getString(R.string.error_required)
            } else {
                addGeofence()
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
        pendingRadius = radius
        radiusDescription.text =
            getString(R.string.radius_description, radius.roundToInt().toString())
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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
