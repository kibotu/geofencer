package net.kibotu.geofencer.demo.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import net.kibotu.geofencer.demo.R

private enum class PermissionStep { Notification, ForegroundLocation, BackgroundLocation, Done }

@Composable
fun PermissionFlow(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(determineInitialStep(context)) }
    var showRationale by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        step = PermissionStep.Done
    }

    val foregroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                step = PermissionStep.BackgroundLocation
            } else {
                step = PermissionStep.Done
            }
        } else {
            showRationale = context.getString(R.string.dialog_rationale_coarse_location)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            step = PermissionStep.ForegroundLocation
        } else {
            step = PermissionStep.ForegroundLocation
        }
    }

    LaunchedEffect(step, retryTrigger) {
        when (step) {
            PermissionStep.Notification -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    step = PermissionStep.ForegroundLocation
                }
            }
            PermissionStep.ForegroundLocation -> {
                foregroundLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            }
            PermissionStep.BackgroundLocation -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    step = PermissionStep.Done
                }
            }
            PermissionStep.Done -> onAllGranted()
        }
    }

    showRationale?.let { message ->
        AlertDialog(
            onDismissRequest = {
                showRationale = null
            },
            title = null,
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = null
                    retryTrigger++
                }) {
                    Text(stringResource(R.string.button_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = null
                    step = PermissionStep.Done
                }) {
                    Text(stringResource(R.string.button_reject))
                }
            },
        )
    }
}

private fun determineInitialStep(context: android.content.Context): PermissionStep {
    val hasLocation = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (hasLocation) return PermissionStep.Done

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val hasNotification = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasNotification) PermissionStep.ForegroundLocation else PermissionStep.Notification
    } else {
        PermissionStep.ForegroundLocation
    }
}
