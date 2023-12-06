package com.sprotte.geolocator.demo.java;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationResult;
import com.sprotte.geolocator.demo.misc.UtilsKt;
import com.sprotte.geolocator.geofencer.models.LocationTrackerUpdateModule;

public class LocationTrackerWorker extends LocationTrackerUpdateModule {

    private Context context;

    LocationTrackerWorker(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void onLocationResult(@NonNull LocationResult locationResult) {
        UtilsKt.sendNotification(
                context,
                "Location Update",
                locationResult.toString()
        );
    }
}
