package com.sprotte.geolocator.demo.java;

import android.util.Log;

import com.sprotte.geolocator.demo.misc.UtilsKt;
import com.sprotte.geolocator.geofencer.models.Geofence;

import org.jetbrains.annotations.NotNull;

public class GeofenceIntentService extends com.sprotte.geolocator.geofencer.service.GeofenceIntentService {

    @Override
    public void onGeofence(@NotNull Geofence geofence) {

        Log.v(GeofenceIntentService.class.getSimpleName(), "onGeofence " + geofence);

        UtilsKt.sendNotification(
                getApplicationContext(),
                geofence.getTitle(),
                geofence.getMessage()
        );
    }
}
