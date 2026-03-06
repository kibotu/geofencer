package net.kibotu.geofencer.demo.kotlin

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BreachMarker(
    val latitude: Double,
    val longitude: Double,
    val geofenceId: String,
    val geofenceLabel: String,
    val transition: String,
    val timestampMillis: Long = System.currentTimeMillis(),
)

object BreachMarkerRepository {

    private const val PREFS_NAME = "breach_markers"
    private const val KEY_MARKERS = "markers"

    private val json = Json { ignoreUnknownKeys = true }

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun add(context: Context, marker: BreachMarker) {
        val all = getAll(context).toMutableList()
        all.add(marker)
        prefs(context).edit {
            putString(KEY_MARKERS, json.encodeToString(all))
        }
    }

    fun getAll(context: Context): List<BreachMarker> {
        val raw = prefs(context).getString(KEY_MARKERS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<BreachMarker>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clear(context: Context) {
        prefs(context).edit { remove(KEY_MARKERS) }
    }
}
