package net.kibotu.geofencer.demo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.kibotu.geofencer.demo.R

@Composable
fun FabColumn(
    showLocationControls: Boolean,
    onStyleClick: () -> Unit,
    onMyLocationClick: () -> Unit,
    onToggleLogClick: () -> Unit,
    onNewReminderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SmallFab(
            onClick = onStyleClick,
            contentDescription = stringResource(R.string.map_style),
        ) {
            Icon(Icons.Default.Layers, contentDescription = null)
        }

        if (showLocationControls) {
            SmallFab(
                onClick = onMyLocationClick,
                contentDescription = stringResource(R.string.map_style),
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
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
                onClick = onNewReminderClick,
                contentDescription = null,
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
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
            .size(36.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        content = content,
    )
}
