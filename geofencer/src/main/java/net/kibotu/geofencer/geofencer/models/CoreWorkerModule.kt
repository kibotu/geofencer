package net.kibotu.geofencer.geofencer.models

import android.content.Context
import net.kibotu.geofencer.geofencer.service.GeoFenceBootInterface
import net.kibotu.geofencer.geofencer.service.GeoFenceUpdateInterface
import net.kibotu.geofencer.geofencer.service.GeofencerInterface
import net.kibotu.geofencer.geofencer.service.LocationTrackerUpdateInterface

abstract class CoreWorkerModule(val context: Context) : GeofencerInterface
abstract class GeoFenceUpdateModule(context: Context) : CoreWorkerModule(context), GeoFenceUpdateInterface
abstract class GeoFenceBootModule(context: Context) : CoreWorkerModule(context), GeoFenceBootInterface
abstract class LocationTrackerUpdateModule(context: Context) : CoreWorkerModule(context), LocationTrackerUpdateInterface
