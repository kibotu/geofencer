package net.kibotu.geofencer.demo.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLng as GmsLatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.DragState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import net.kibotu.geofencer.demo.R
import net.kibotu.geofencer.demo.kotlin.BreachMarker
import net.kibotu.geofencer.Geofence
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val EARTH_RADIUS_METERS = 6_371_000.0

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

private enum class BreachType(val tag: String, val snippet: String, val color: Int) {
    ENTER("ENTER", "Entered fence", 0xFF2E7D32.toInt()),
    EXIT("EXIT", "Exited fence", 0xFFC62828.toInt()),
    DWELL("DWELL", "Dwelling in fence", 0xFFF57F17.toInt()),
}

@Composable
fun BreachMarkerContent(breach: BreachMarker) {
    val context = LocalContext.current
    val position = if (breach.geofenceRadius > 0.0) {
        projectOntoFence(
            center = LatLng(breach.geofenceLatitude, breach.geofenceLongitude),
            detected = LatLng(breach.latitude, breach.longitude),
            radiusMeters = breach.geofenceRadius,
        )
    } else {
        LatLng(breach.latitude, breach.longitude)
    }
    val type = when {
        breach.transition.equals("Enter", ignoreCase = true) -> BreachType.ENTER
        breach.transition.equals("Dwell", ignoreCase = true) -> BreachType.DWELL
        else -> BreachType.EXIT
    }
    val fenceName = breach.geofenceLabel
    val icon = rememberBreachIcon(
        context = context,
        type = type,
        label = fenceName,
    )

    Marker(
        state = rememberUpdatedMarkerState(position = position),
        title = fenceName,
        snippet = type.snippet,
        icon = icon,
    )
}

@Composable
fun DraggableWizardMarker(
    position: GmsLatLng,
    radius: Double,
    onDragEnd: (GmsLatLng) -> Unit,
) {
    val context = LocalContext.current
    val icon = rememberVectorIcon(context, R.drawable.ic_twotone_location_on_48px)
    val markerState = rememberMarkerState(position = position)

    LaunchedEffect(position) {
        markerState.position = position
    }

    LaunchedEffect(markerState) {
        snapshotFlow { markerState.dragState }
            .collect { state ->
                if (state == DragState.END) {
                    onDragEnd(markerState.position)
                }
            }
    }

    Marker(
        state = markerState,
        icon = icon,
        draggable = true,
    )

    Circle(
        center = markerState.position,
        radius = radius,
        strokeColor = geofenceStroke,
        fillColor = geofenceFill,
    )
}

/**
 * Projects a detected location onto the geofence circle perimeter along the
 * bearing from [center] to [detected], returning the point at [radiusMeters]
 * from the center in that direction. Falls back to [detected] when center and
 * detected coincide (bearing is undefined).
 */
private fun projectOntoFence(
    center: LatLng,
    detected: LatLng,
    radiusMeters: Double,
): LatLng {
    val lat1 = Math.toRadians(center.latitude)
    val lon1 = Math.toRadians(center.longitude)
    val lat2 = Math.toRadians(detected.latitude)
    val lon2 = Math.toRadians(detected.longitude)
    val dLon = lon2 - lon1

    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    val bearing = atan2(y, x)

    if (x == 0.0 && y == 0.0) return detected

    val angularDistance = radiusMeters / EARTH_RADIUS_METERS
    val destLat = asin(
        sin(lat1) * cos(angularDistance) +
            cos(lat1) * sin(angularDistance) * cos(bearing),
    )
    val destLon = lon1 + atan2(
        sin(bearing) * sin(angularDistance) * cos(lat1),
        cos(angularDistance) - sin(lat1) * sin(destLat),
    )

    return LatLng(Math.toDegrees(destLat), Math.toDegrees(destLon))
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

@Composable
private fun rememberBreachIcon(
    context: Context,
    type: BreachType,
    label: String,
): BitmapDescriptor {
    val iconRes = when (type) {
        BreachType.ENTER -> R.drawable.ic_breach_enter
        BreachType.EXIT -> R.drawable.ic_breach_exit
        BreachType.DWELL -> R.drawable.ic_breach_dwell
    }
    return remember(type, label) {
        createBreachBitmap(context, iconRes, type, label)
    }
}

private fun createBreachBitmap(
    context: Context,
    @DrawableRes iconRes: Int,
    type: BreachType,
    label: String,
): BitmapDescriptor {
    val density = context.resources.displayMetrics.density

    val pinDrawable = ResourcesCompat.getDrawable(context.resources, iconRes, null)!!
    val pinW = pinDrawable.intrinsicWidth
    val pinH = pinDrawable.intrinsicHeight

    val displayText = label.ifEmpty { type.tag }
    val truncated = if (displayText.length > 24) displayText.take(21) + "…" else displayText

    val textSize = 11f * density
    val tagPadH = 6f * density
    val tagPadV = 3f * density
    val tagRadius = 4f * density
    val tagMarginBottom = 4f * density

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = android.graphics.Color.WHITE
    }
    val textWidth = textPaint.measureText(truncated)
    val textMetrics = textPaint.fontMetrics
    val textHeight = textMetrics.descent - textMetrics.ascent

    val pillW = textWidth + tagPadH * 2
    val pillH = textHeight + tagPadV * 2

    val totalW = maxOf(pinW.toFloat(), pillW).toInt()
    val totalH = (pillH + tagMarginBottom + pinH).toInt()

    val bitmap = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = type.color }
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x40000000
        setShadowLayer(2f * density, 0f, 1f * density, 0x40000000)
    }

    val pillLeft = (totalW - pillW) / 2f
    val pillRect = RectF(pillLeft, 0f, pillLeft + pillW, pillH)
    canvas.drawRoundRect(pillRect, tagRadius, tagRadius, shadowPaint)
    canvas.drawRoundRect(pillRect, tagRadius, tagRadius, pillPaint)

    val textX = pillLeft + tagPadH
    val textY = tagPadV - textMetrics.ascent
    canvas.drawText(truncated, textX, textY, textPaint)

    val pinLeft = (totalW - pinW) / 2
    val pinTop = (pillH + tagMarginBottom).toInt()
    pinDrawable.setBounds(pinLeft, pinTop, pinLeft + pinW, pinTop + pinH)
    pinDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
