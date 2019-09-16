package com.sprotte.geolocator.demo.java;

import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.sprotte.geolocator.demo.misc.UtilsKt;
import com.sprotte.geolocator.tracking.service.LocationTrackerUpdateIntentService;

import org.jetbrains.annotations.NotNull;

public class LocationTrackerService extends LocationTrackerUpdateIntentService {

    @Override
    public void onLocationResult(@NotNull LocationResult location) {

        Log.v(GeofenceIntentService.class.getSimpleName(), "onLocationResult " + location);

        UtilsKt.sendNotification(
                getApplicationContext(),
                "Location Update",
                location.toString()
        );
    }
}