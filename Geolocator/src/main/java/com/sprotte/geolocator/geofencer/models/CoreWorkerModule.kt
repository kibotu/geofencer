package com.sprotte.geolocator.geofencer.models

import android.content.Context
import com.sprotte.geolocator.geofencer.service.GeoFenceBootInterface
import com.sprotte.geolocator.geofencer.service.GeoFenceUpdateInterface
import com.sprotte.geolocator.geofencer.service.GeoLocatorInterface
import com.sprotte.geolocator.geofencer.service.LocationTrackerUpdateInterface

abstract class CoreWorkerModule(context: Context) : GeoLocatorInterface
abstract class GeoFenceUpdateModule(context: Context) : GeoFenceUpdateInterface
abstract class GeoFenceBootModule(context: Context) : GeoFenceBootInterface
abstract class LocationTrackerUpdateModule(context: Context) : LocationTrackerUpdateInterface
