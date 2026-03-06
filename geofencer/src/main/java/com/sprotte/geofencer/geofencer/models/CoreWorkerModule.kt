package com.sprotte.geofencer.geofencer.models

import android.content.Context
import com.sprotte.geofencer.geofencer.service.GeoFenceBootInterface
import com.sprotte.geofencer.geofencer.service.GeoFenceUpdateInterface
import com.sprotte.geofencer.geofencer.service.GeofencerInterface
import com.sprotte.geofencer.geofencer.service.LocationTrackerUpdateInterface

abstract class CoreWorkerModule(val context: Context) : GeofencerInterface
abstract class GeoFenceUpdateModule(context: Context) : CoreWorkerModule(context), GeoFenceUpdateInterface
abstract class GeoFenceBootModule(context: Context) : CoreWorkerModule(context), GeoFenceBootInterface
abstract class LocationTrackerUpdateModule(context: Context) : CoreWorkerModule(context), LocationTrackerUpdateInterface
