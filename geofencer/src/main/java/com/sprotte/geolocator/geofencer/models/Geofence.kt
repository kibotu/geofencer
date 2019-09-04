
package com.sprotte.geolocator.geofencer.models

import java.util.*

data class Geofence(val id: String = UUID.randomUUID().toString(),
                    var latitude: Double = 0.0,
                    var longitude: Double = 0.0,
                    var radius: Double = 0.0,
                    var title: String = "",
                    var message: String = "",
                    var transitionType: Int = 1){

    var intentClassName: String = ""
}