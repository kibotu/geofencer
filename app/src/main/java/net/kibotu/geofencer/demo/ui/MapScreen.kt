package net.kibotu.geofencer.demo.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import net.kibotu.geofencer.demo.R
import net.kibotu.geofencer.demo.ui.components.BreachMarkerContent
import net.kibotu.geofencer.demo.ui.components.EventLogSheet
import net.kibotu.geofencer.demo.ui.components.FabColumn
import net.kibotu.geofencer.demo.ui.components.GeofenceMapContent
import net.kibotu.geofencer.demo.ui.components.GeofenceWizardOverlay
import net.kibotu.geofencer.demo.ui.components.MapStylePickerDialog
import net.kibotu.geofencer.demo.ui.components.PermissionFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showStylePicker by remember { mutableStateOf(false) }
    var permissionsDone by remember { mutableStateOf(false) }

    val hasLocationPermission = remember(permissionsDone) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    if (!permissionsDone) {
        PermissionFlow(
            onAllGranted = {
                permissionsDone = true
                viewModel.onPermissionsGranted()
            },
        )
    }

    LaunchedEffect(permissionsDone) {
        if (permissionsDone) {
            val latLng = viewModel.getLastKnownLatLng()
            if (latLng != null) {
                val cameraPosition = CameraPosition.Builder()
                    .target(latLng)
                    .zoom(17f)
                    .tilt(45f)
                    .bearing(0f)
                    .build()
                cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }
    }

    val mapProperties = remember(viewModel.currentMapStyle, hasLocationPermission) {
        val styleOptions = viewModel.currentMapStyle.styleRes?.let {
            MapStyleOptions.loadRawResourceStyle(context, it)
        }
        MapProperties(
            mapType = viewModel.currentMapStyle.composeMapType,
            isBuildingEnabled = true,
            mapStyleOptions = styleOptions,
            isMyLocationEnabled = hasLocationPermission,
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            zoomControlsEnabled = true,
        )
    }

    var sheetVisible by remember { mutableStateOf(false) }
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val sheetPeekHeight = if (sheetVisible) 56.dp else 0.dp

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetDragHandle = if (sheetVisible) null else (@Composable {}),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetContent = {
            if (sheetVisible) {
                EventLogSheet(
                    entries = viewModel.logEntries,
                    onClear = { viewModel.clearLog() },
                )
            }
        },
    ) { contentPadding ->
        Box(Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                contentPadding = PaddingValues(
                    bottom = contentPadding.calculateBottomPadding(),
                ),
            ) {
                for (geofence in viewModel.geofences) {
                    GeofenceMapContent(
                        geofence = geofence,
                        onClick = { id -> viewModel.onMarkerClicked(id) },
                    )
                }
                for (breach in viewModel.breachMarkers) {
                    BreachMarkerContent(breach)
                }
                viewModel.wizardPreview?.let { preview ->
                    GeofenceMapContent(geofence = preview)
                }
            }

            FabColumn(
                showLocationControls = hasLocationPermission,
                onStyleClick = { showStylePicker = true },
                onMyLocationClick = {
                    val latLng = viewModel.getLastKnownLatLng()
                    if (latLng != null) {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                            )
                        }
                    }
                },
                onToggleLogClick = {
                    if (sheetVisible) {
                        sheetVisible = false
                    } else {
                        sheetVisible = true
                        scope.launch { sheetState.partialExpand() }
                    }
                },
                onNewReminderClick = {
                    viewModel.startWizard(cameraPositionState.position.target)
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(end = 8.dp),
            )

            GeofenceWizardOverlay(
                state = viewModel.wizardState,
                onConfirmLocation = {
                    viewModel.confirmLocation(cameraPositionState.position.target)
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomTo(15f))
                    }
                },
                onRadiusChanged = { viewModel.updateRadius(it) },
                onConfirmRadius = { viewModel.confirmRadius() },
                onMessageChanged = { viewModel.updateMessage(it) },
                onSubmit = { viewModel.submitGeofence() },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    // Map style picker dialog
    if (showStylePicker) {
        MapStylePickerDialog(
            current = viewModel.currentMapStyle,
            onSelect = {
                viewModel.setMapStyle(it)
                showStylePicker = false
            },
            onDismiss = { showStylePicker = false },
        )
    }

    // Geofence removal confirmation dialog
    viewModel.markerToRemove?.let { geofence ->
        val removedMsg = stringResource(R.string.reminder_removed_success)
        AlertDialog(
            onDismissRequest = { viewModel.dismissRemoveDialog() },
            text = { Text(stringResource(R.string.reminder_removal_alert)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.confirmRemoveGeofence()
                    scope.launch { snackbarHostState.showSnackbar(removedMsg) }
                }) {
                    Text(stringResource(R.string.reminder_removal_alert_positive))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRemoveDialog() }) {
                    Text(stringResource(R.string.reminder_removal_alert_negative))
                }
            },
        )
    }
}
