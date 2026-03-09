package net.kibotu.geofencer.internal

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import net.kibotu.geofencer.Geofence
import net.kibotu.geofencer.GeofenceAction
import net.kibotu.geofencer.GeofenceEvent
import net.kibotu.geofencer.Geofencer
import net.kibotu.geofencer.LatLng
import timber.log.Timber

internal class GeofenceActionWorker(
    private val ctx: Context,
    params: WorkerParameters,
) : Worker(ctx, params) {

    override fun doWork(): Result {
        return try {
            val id = inputData.getString(Extras.GEOFENCE_ID) ?: return Result.failure()
            val geofence = Geofencer.getRepository(ctx).get(id) ?: return Result.failure()
            val transitionType = inputData.getInt(Extras.TRANSITION_TYPE, -1)
            val trigLat = inputData.getDouble(Extras.TRIGGERING_LAT, Double.NaN)
            val trigLng = inputData.getDouble(Extras.TRIGGERING_LNG, Double.NaN)
            dispatch(geofence, transitionType, trigLat, trigLng)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "GeofenceActionWorker failed")
            Result.failure()
        }
    }

    private fun dispatch(
        geofence: Geofence,
        transitionType: Int,
        triggeringLatitude: Double,
        triggeringLongitude: Double,
    ) {
        val className = geofence.actionClass
        if (className.isEmpty()) return

        try {
            val clazz = Class.forName(className)
            if (!GeofenceAction::class.java.isAssignableFrom(clazz)) return

            val action = clazz.getDeclaredConstructor().newInstance() as GeofenceAction
            val transition = Geofence.Transition.of(transitionType) ?: Geofence.Transition.Enter
            val location = if (!triggeringLatitude.isNaN() && !triggeringLongitude.isNaN()) {
                LatLng(triggeringLatitude, triggeringLongitude)
            } else {
                null
            }
            val event = GeofenceEvent(
                geofence = geofence,
                transition = transition,
                triggeringLocation = location,
            )
            action.onTriggered(applicationContext, event)
        } catch (e: Exception) {
            Timber.e(e, "Failed to dispatch action for geofence ${geofence.id}")
        }
    }
}
