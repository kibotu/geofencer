package com.sprotte.geolocator.demo.java;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sprotte.geolocator.demo.misc.UtilsKt;
import com.sprotte.geolocator.geofencer.models.GeoFenceUpdateModule;
import com.sprotte.geolocator.geofencer.models.Geofence;

import timber.log.Timber;

public class NotificationWorker extends GeoFenceUpdateModule {

    private Context context;
    NotificationWorker(Context context){
        super(context);
        this.context = context;

    }

    @Override
    public void onGeofence(@NonNull Geofence geofence) {
        Timber.d("NotificationWorker JAVA : onGeofence ");
        UtilsKt.sendNotification(context, geofence.getTitle(), geofence.getMessage());
    }
}
