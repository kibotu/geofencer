package net.kibotu.geofencer.demo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.kibotu.geofencer.demo.R

private val RecordingRed = Color(0xFFE53935)
private val StoppedGray = Color(0xFF757575)

@Composable
fun FabColumn(
    showLocationControls: Boolean,
    isHighFrequency: Boolean,
    isTracking: Boolean,
    onHighFrequencyToggle: () -> Unit,
    onTrackingToggle: () -> Unit,
    onStyleClick: () -> Unit,
    onMyLocationClick: () -> Unit,
    onToggleLogClick: () -> Unit,
    onNewReminderClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        if (showLocationControls) {
            TrackingFab(
                isTracking = isTracking,
                onClick = onTrackingToggle,
            )
        }

        SmallFab(
            onClick = onStyleClick,
            contentDescription = stringResource(R.string.map_style),
        ) {
            Icon(Icons.Default.Layers, contentDescription = null)
        }

        if (showLocationControls) {
            SmallFab(
                onClick = onMyLocationClick,
                contentDescription = "My location",
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
            }

            SmallFab(
                onClick = onHighFrequencyToggle,
                contentDescription = stringResource(R.string.high_frequency_tracking),
            ) {
                Icon(
                    imageVector = if (isHighFrequency) Icons.Default.Speed else Icons.Default.BatterySaver,
                    contentDescription = null,
                )
            }
        }

        SmallFab(
            onClick = onToggleLogClick,
            contentDescription = stringResource(R.string.toggle_event_log),
        ) {
            Icon(Icons.Default.Timeline, contentDescription = null)
        }

        if (showLocationControls) {
            SmallFab(
                onClick = onSearchClick,
                contentDescription = stringResource(R.string.search_street),
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
            }

            SmallFab(
                onClick = onNewReminderClick,
                contentDescription = "Add geofence",
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}

@Composable
private fun TrackingFab(
    isTracking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isTracking) RecordingRed else StoppedGray,
        animationSpec = tween(300),
        label = "trackingColor",
    )

    val pulseAlpha = if (isTracking) {
        val transition = rememberInfiniteTransition(label = "pulse")
        val alpha by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulseAlpha",
        )
        alpha
    } else {
        1f
    }

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .padding(bottom = 6.dp)
            .size(42.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Toggle location tracking"
            },
        containerColor = containerColor,
        contentColor = Color.White,
        shape = CircleShape,
    ) {
        val indicatorShape = if (isTracking) CircleShape else RoundedCornerShape(4.dp)
        val indicatorSize = if (isTracking) 16.dp else 14.dp
        Box(
            modifier = Modifier
                .size(indicatorSize)
                .alpha(pulseAlpha)
                .background(Color.White, indicatorShape),
        )
    }
}

@Composable
private fun SmallFab(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .padding(bottom = 6.dp)
            .size(36.dp)
            .then(
                if (contentDescription != null) {
                    val desc = contentDescription
                    Modifier.semantics(mergeDescendants = true) {
                        this.contentDescription = desc
                    }
                } else Modifier,
            ),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        content = content,
    )
}
