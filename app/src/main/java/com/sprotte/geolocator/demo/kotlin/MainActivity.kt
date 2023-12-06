package com.sprotte.geolocator.demo.kotlin

import android.Manifest.permission
import android.os.Bundle
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.sprotte.geolocator.demo.R
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.geofencer.models.Geofence
import com.sprotte.geolocator.tracking.LocationTracker
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        requestLocationPermission {
//            if (it.granted) {
//                registerGeofenceUpdates()
//                registerLocationUpdateEvents()
//            }
//        }
    }

    override fun onSupportNavigateUp() = Navigation.findNavController(this, R.id.navHost).navigateUp() || super.onSupportNavigateUp()
}
