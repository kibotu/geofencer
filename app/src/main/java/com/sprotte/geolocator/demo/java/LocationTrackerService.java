package com.sprotte.geolocator.demo.java;

import com.google.android.gms.location.LocationResult;
import com.sprotte.geolocator.demo.misc.UtilsKt;
import com.sprotte.geolocator.tracking.service.LocationTrackerUpdateIntentService;

import org.jetbrains.annotations.NotNull;

public class LocationTrackerService extends LocationTrackerUpdateIntentService {

    @Override
    public void onLocationResult(@NotNull LocationResult locationResult) {
        UtilsKt.sendNotification(
                getApplicationContext(),
                "Location Update",
                locationResult.toString()
        );
    }
}