package net.kibotu.geofencer.demo.kotlin

import androidx.annotation.RawRes
import com.google.android.gms.maps.GoogleMap
import net.kibotu.geofencer.demo.R

enum class MapStyle(
    val displayName: String,
    @RawRes val styleRes: Int? = null,
    val mapType: Int = GoogleMap.MAP_TYPE_NORMAL
) {
    POKEMON_GO("Pokemon GO", styleRes = R.raw.map_style_pokemon_go),
    STEAMPUNK("Steampunk", styleRes = R.raw.map_style_steampunk),
    LIGHT("Light", styleRes = R.raw.map_style_light),
    DARK("Dark 3D", styleRes = R.raw.map_style_satellite_dark),
    SATELLITE("Satellite", mapType = GoogleMap.MAP_TYPE_HYBRID);

    companion object {
        fun fromName(name: String): MapStyle =
            entries.firstOrNull { it.name == name } ?: POKEMON_GO
    }
}
