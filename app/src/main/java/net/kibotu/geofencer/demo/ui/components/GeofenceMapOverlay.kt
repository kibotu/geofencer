package net.kibotu.geofencer.demo.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import net.kibotu.geofencer.demo.R
import net.kibotu.geofencer.demo.kotlin.BreachMarker
import net.kibotu.geofencer.Geofence

private val geofenceStroke = Color(0xFF2594E4)
private val geofenceFill = Color(0x4008AB91)

@Composable
fun GeofenceMapContent(
    geofence: Geofence,
    onClick: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val position = LatLng(geofence.latitude, geofence.longitude)
    val icon = rememberVectorIcon(context, R.drawable.ic_twotone_location_on_48px)

    Marker(
        state = rememberUpdatedMarkerState(position = position),
        tag = geofence.id,
        icon = icon,
        onClick = {
            onClick?.invoke(geofence.id)
            true
        },
    )
    Circle(
        center = position,
        radius = geofence.radius,
        strokeColor = geofenceStroke,
        fillColor = geofenceFill,
    )
}

@Composable
fun BreachMarkerContent(breach: BreachMarker) {
    val context = LocalContext.current
    val position = LatLng(breach.latitude, breach.longitude)
    val icon = rememberVectorIcon(context, R.drawable.ic_breach_marker)
    val label = breach.geofenceLabel.ifEmpty { breach.geofenceId.take(8) }

    Marker(
        state = rememberUpdatedMarkerState(position = position),
        title = "${breach.transition}: $label",
        icon = icon,
    )
}

@Composable
private fun rememberVectorIcon(context: Context, @DrawableRes id: Int): BitmapDescriptor {
    return remember(id) {
        val drawable = ResourcesCompat.getDrawable(context.resources, id, null)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
