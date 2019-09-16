package com.sprotte.geolocator.geofencer

import androidx.annotation.IntDef
import com.google.android.gms.location.Geofence.*

@IntDef(GEOFENCE_TRANSITION_DWELL, GEOFENCE_TRANSITION_ENTER, GEOFENCE_TRANSITION_EXIT)
@Retention(AnnotationRetention.SOURCE)
annotation class TransitionType