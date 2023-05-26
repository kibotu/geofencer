package com.sprotte.geolocator.geofencer.models

import android.os.Bundle

internal data class WorkModuleInfo(
    val moduleClass: Class<out WorkerModule>,
    val metaData: Bundle?
)