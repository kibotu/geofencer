package com.sprotte.geolocator.demo.java;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import com.sprotte.geolocator.demo.R;
import com.sprotte.geolocator.demo.misc.UtilsKt;
import com.sprotte.geolocator.geofencer.Geofencer;
import com.sprotte.geolocator.geofencer.models.Geofence;
import com.sprotte.geolocator.tracking.LocationTracker;


import java.util.UUID;

import kotlin.Unit;

import static com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER;

public class AddGeoFenceActivity extends AppCompatActivity {

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        UtilsKt.requestLocationPermission(this, permission ->
        {
            if (permission.granted) {
                registerGeofenceUpdates();
                registerLocationUpdateEvents();
            }
            return Unit.INSTANCE;
        });
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private void registerGeofenceUpdates() {

        Geofence geofence = new Geofence(
                UUID.randomUUID().toString(),
                51.0899232,
                5.968358,
                30.0,
                "Germany",
                "Entered Germany",
                GEOFENCE_TRANSITION_ENTER);
        Geofencer geofencer = new Geofencer(this);
        geofencer.addGeofenceWorker(geofence, NotificationWorker.class, ()-> Unit.INSTANCE);
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private void registerLocationUpdateEvents() {
        LocationTracker.INSTANCE.requestLocationUpdates(this, LocationTrackerWorker.class);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.navHost).navigateUp() || super.onSupportNavigateUp();
    }
}