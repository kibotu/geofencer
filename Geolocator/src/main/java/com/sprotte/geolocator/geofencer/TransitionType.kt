package com.sprotte.geolocator.geofencer

import androidx.annotation.IntDef
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT

@IntDef(GEOFENCE_TRANSITION_DWELL, GEOFENCE_TRANSITION_ENTER, GEOFENCE_TRANSITION_EXIT)
@Retention(AnnotationRetention.SOURCE)
annotation class TransitionType