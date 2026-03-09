package net.kibotu.geofencer.internal

internal object Prefs {
    const val GEOFENCE_PREFS = "GeofenceRepository"
    const val GEOFENCE_KEY = "geofences_json"
    const val LOCATION_PREFS = "net.kibotu.geofencer.LocationTracker"
    const val LOCATION_ACTION_KEY = "location_action_class"
}

internal object Extras {
    const val GEOFENCE_ID = "geofence_id"
    const val TRANSITION_TYPE = "geofence_transition_type"
    const val TRIGGERING_LAT = "geofence_triggering_lat"
    const val TRIGGERING_LNG = "geofence_triggering_lng"
    const val LOCATION_ACTION_CLASS = "location_action_class_name"
    const val LOCATION_INTENT = "location_intent_uri"
    const val REQUEST_CODE = 5999
}
