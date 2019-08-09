package com.sprotte.geofencer.demo.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.exozet.android.core.base.BaseFragment
import com.github.florent37.application.provider.application
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.sprotte.geofencer.demo.R
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxkotlin.addTo
import net.kibotu.logger.Logger.logv

class MapFragment : BaseFragment(), GoogleMap.OnMarkerClickListener {

    override val layout = R.layout.fragment_map

    private var map: GoogleMap? = null

    private lateinit var locationManager: LocationManager

    override fun subscribeUi() {
        super.subscribeUi()

        locationManager = application?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment


        mapFragment.getMapAsync { map ->

            this.map = map

            logv { "map = $map" }

            requestLocationPermission {
                map.isMyLocationEnabled = it.granted

                val bestProvider = locationManager.getBestProvider(Criteria(), false)
                if (it.granted) {
                    @SuppressLint("MissingPermission")
                    val location = locationManager.getLastKnownLocation(bestProvider)
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
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

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
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
}

