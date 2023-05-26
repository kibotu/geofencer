package com.sprotte.geolocator.tracking

import android.content.Context
import com.google.android.gms.location.Priority
import com.sprotte.geolocator.R
import com.sprotte.geolocator.utils.getRes

/**
 * Created by [AgnaldoNP](https://github.com/AgnaldoNP).
 *
 * https://codelabs.developers.google.com/codelabs/background-location-updates-android-o/
 * https://github.com/googlesamples/android-play-location
 */
open class LocationTrackerParams {
    /**
     * Sets the desired interval for active location updates. This interval is
     * inexact. You may not receive updates at all if no location sources are available, or
     * you may receive them slower than requested. You may also receive updates faster than
     * requested if other applications are requesting location at a faster interval.
     * Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
     * less frequently than this interval when the app is no longer in the foreground.
     */
    var interval: Long = 0

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    var fastestInterval: Long = 0

    var priority: Int = 0

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    var maxWaitTime: Long = 0

    /**
     * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest#setSmallestDisplacement(float)
     */
    var smallestDisplacement: Float = 0.toFloat()

    constructor(context: Context) {
        this.interval = context.getRes(R.integer.location_update_interval_in_millis)
        this.fastestInterval = context.getRes(R.integer.location_fastest_update_interval_in_millis)
        this.priority = Priority.PRIORITY_HIGH_ACCURACY
        this.maxWaitTime = context.getRes(R.integer.location_max_wait_time_interval_in_millis)
        this.smallestDisplacement =
            context.getRes(R.integer.location_min_distance_for_updates_in_meters).toFloat()
    }

    constructor(
        interval: Long,
        fastestInterval: Long,
        priority: Int,
        maxWaitTime: Long,
        smallestDisplacement: Float
    ) {
        this.interval = interval
        this.fastestInterval = fastestInterval
        this.priority = priority
        this.maxWaitTime = maxWaitTime
        this.smallestDisplacement = smallestDisplacement
    }
}

