package net.kibotu.geofencer.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.android.gms.location.LocationResult
import net.kibotu.geofencer.Geofencer
import net.kibotu.geofencer.LocationTracker
import timber.log.Timber

class LocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        if (ACTION_PROCESS_UPDATES != intent.action) return

        val result = try {
            LocationResult.extractResult(intent) ?: return
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract location result")
            return
        }

        LocationTracker.mutableRawResults.tryEmit(result)

        val prefs = context.getSharedPreferences(Prefs.LOCATION_PREFS, Context.MODE_PRIVATE)
        val pipeline = LocationFilterPipeline(buildFilters(prefs))
        val history = LocationTracker.history
        val actionClass = prefs.getString(Prefs.LOCATION_ACTION_KEY, "")

        var anyAccepted = false

        for (location in result.locations) {
            if (!pipeline.accept(location, history)) {
                Timber.d("Filtered location: provider=%s accuracy=%.1fm", location.provider, location.accuracy)
                continue
            }

            history.add(location)
            LocationTracker.mutableLocations.tryEmit(location)
            anyAccepted = true

            evaluateGeofences(context, location)
        }

        if (!actionClass.isNullOrEmpty() && anyAccepted) {
            Workers.enqueueLocationAction(context, actionClass, intent.toUri(0))
        }
    }

    private fun evaluateGeofences(context: Context, location: android.location.Location) {
        val geofences = try {
            Geofencer.getRepository(context).getAll()
        } catch (_: Exception) {
            return
        }
        if (geofences.isEmpty()) return

        val state = GeofenceEvaluationState
        val candidateEvents = state.evaluator.evaluate(location, geofences)

        for (candidate in candidateEvents) {
            val confirmed = state.dwellConfirmation.process(candidate) ?: continue
            if (!state.deduplicator.shouldEmit(confirmed)) continue
            Geofencer.mutableEvents.tryEmit(confirmed)
        }
    }

    companion object {
        internal const val ACTION_PROCESS_UPDATES =
            "net.kibotu.geofencer.internal.ACTION_PROCESS_UPDATES"
    }
}

private fun buildFilters(prefs: SharedPreferences): List<LocationFilter> = buildList {
    val maxAccuracy = prefs.getFloat(Prefs.LOCATION_MAX_ACCURACY_KEY, 0f)
    if (maxAccuracy > 0f) add(AccuracyFilter(maxAccuracy))

    val maxSpeed = prefs.getFloat(Prefs.LOCATION_MAX_SPEED_KEY, 55f)
    if (maxSpeed > 0f) add(SpeedFilter(maxSpeed))

    val accuracyWeighted = prefs.getBoolean(Prefs.LOCATION_ACCURACY_WEIGHTED_KEY, true)
    if (accuracyWeighted) add(AccuracyWeightedFilter())

    val minNetworkAccuracy = prefs.getFloat(Prefs.LOCATION_MIN_NETWORK_ACCURACY_KEY, 50f)
    if (minNetworkAccuracy > 0f) add(ProviderQualityFilter(minNetworkAccuracy))
}
