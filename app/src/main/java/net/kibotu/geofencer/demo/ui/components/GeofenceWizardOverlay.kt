package net.kibotu.geofencer.demo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.kibotu.geofencer.demo.R
import net.kibotu.geofencer.demo.ui.WizardState
import kotlin.math.roundToInt

@Composable
fun GeofenceWizardOverlay(
    state: WizardState,
    onConfirmLocation: () -> Unit,
    onRadiusChanged: (Double) -> Unit,
    onConfirmRadius: () -> Unit,
    onMessageChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = state !is WizardState.Hidden,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp),
            ) {
                when (state) {
                    is WizardState.PickLocation -> LocationStep(
                        onConfirm = onConfirmLocation,
                        onCancel = onCancel,
                    )
                    is WizardState.PickRadius -> RadiusStep(
                        radius = state.radius,
                        onRadiusChanged = onRadiusChanged,
                        onConfirm = onConfirmRadius,
                        onCancel = onCancel,
                    )
                    is WizardState.PickMessage -> MessageStep(
                        message = state.message,
                        onMessageChanged = onMessageChanged,
                        onSubmit = onSubmit,
                        onCancel = onCancel,
                    )
                    WizardState.Hidden -> {}
                }
            }
        }
    }
}

@Composable
private fun LocationStep(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Text(
        text = stringResource(R.string.instruction_where_description),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = stringResource(R.string.instruction_where_subtitle_description),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
    )
    ButtonRow(onConfirm = onConfirm, onCancel = onCancel)
}

@Composable
private fun RadiusStep(
    radius: Double,
    onRadiusChanged: (Double) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Text(
        text = stringResource(R.string.instruction_radius_description),
        style = MaterialTheme.typography.titleMedium,
    )

    val sliderPosition = ((radius - 10.0) / 990.0).coerceIn(0.0, 1.0).toFloat()
    Slider(
        value = sliderPosition,
        onValueChange = { progress ->
            val r = 10.0 + progress.toDouble() * 990.0
            onRadiusChanged(r)
        },
        valueRange = 0f..1f,
        modifier = Modifier.padding(vertical = 8.dp),
    )
    Text(
        text = stringResource(R.string.radius_description, radius.roundToInt().toString()),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 16.dp),
    )
    ButtonRow(onConfirm = onConfirm, onCancel = onCancel)
}

@Composable
private fun MessageStep(
    message: String,
    onMessageChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    Text(
        text = stringResource(R.string.instruction_message_description),
        style = MaterialTheme.typography.titleMedium,
    )

    var showError by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = message,
        onValueChange = {
            onMessageChanged(it)
            showError = false
        },
        label = { Text(stringResource(R.string.message_hint)) },
        isError = showError,
        supportingText = if (showError) {
            { Text(stringResource(R.string.error_required)) }
        } else null,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    )
    ButtonRow(
        onConfirm = {
            if (message.isBlank()) {
                showError = true
            } else {
                onSubmit()
            }
        },
        onCancel = onCancel,
    )
}

@Composable
private fun ButtonRow(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.cancel_wizard))
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.continue_description))
        }
    }
}
