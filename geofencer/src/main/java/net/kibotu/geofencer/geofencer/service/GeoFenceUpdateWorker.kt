package net.kibotu.geofencer.geofencer.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import net.kibotu.geofencer.geofencer.GeofenceAction
import net.kibotu.geofencer.geofencer.GeofenceEvent
import net.kibotu.geofencer.geofencer.Geofencer
import net.kibotu.geofencer.geofencer.models.Geofence
import timber.log.Timber

internal class GeoFenceUpdateWorker(
    private val ctx: Context,
    params: WorkerParameters,
) : Worker(ctx, params) {

    override fun doWork(): Result {
        return try {
            val id = inputData.getString(Geofencer.INTENT_EXTRAS_KEY) ?: return Result.failure()
            val geofence = Geofencer.getRepository(ctx).get(id) ?: return Result.failure()
            val transitionType = inputData.getInt(Geofencer.EXTRA_TRANSITION_TYPE, -1)
            val trigLat = inputData.getDouble(Geofencer.EXTRA_TRIGGERING_LAT, Double.NaN)
            val trigLng = inputData.getDouble(Geofencer.EXTRA_TRIGGERING_LNG, Double.NaN)
            dispatch(geofence, transitionType, trigLat, trigLng)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "GeoFenceUpdateWorker failed")
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
            val transition = Geofence.Transition.of(transitionType)
                ?: Geofence.Transition.of(geofence.transitions)
                ?: Geofence.Transition.Enter
            val event = GeofenceEvent(
                geofence = geofence,
                transition = transition,
                triggeringLatitude = triggeringLatitude,
                triggeringLongitude = triggeringLongitude,
            )
            action.onTriggered(applicationContext, event)
        } catch (e: Exception) {
            Timber.e(e, "Failed to dispatch action for geofence ${geofence.id}")
        }
    }
}
