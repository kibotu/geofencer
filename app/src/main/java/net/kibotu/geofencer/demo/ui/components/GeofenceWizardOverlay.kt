package net.kibotu.geofencer.demo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
                    is WizardState.PickLocation -> LocationStep(onConfirm = onConfirmLocation)
                    is WizardState.PickRadius -> RadiusStep(
                        radius = state.radius,
                        onRadiusChanged = onRadiusChanged,
                        onConfirm = onConfirmRadius,
                    )
                    is WizardState.PickMessage -> MessageStep(
                        message = state.message,
                        onMessageChanged = onMessageChanged,
                        onSubmit = onSubmit,
                    )
                    WizardState.Hidden -> {}
                }
            }
        }
    }
}

@Composable
private fun LocationStep(onConfirm: () -> Unit) {
    Text(
        text = stringResource(R.string.instruction_where_description),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = stringResource(R.string.instruction_where_subtitle_description),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
    )
    ContinueButton(onClick = onConfirm)
}

@Composable
private fun RadiusStep(
    radius: Double,
    onRadiusChanged: (Double) -> Unit,
    onConfirm: () -> Unit,
) {
    Text(
        text = stringResource(R.string.instruction_radius_description),
        style = MaterialTheme.typography.titleMedium,
    )

    val sliderPosition = ((radius - 100.0) / 200.0 - 0.5).coerceIn(0.0, 4.0).toFloat()
    Slider(
        value = sliderPosition,
        onValueChange = { progress ->
            val r = 100 + (2 * progress.toDouble() + 1) * 100
            onRadiusChanged(r)
        },
        valueRange = 0f..4f,
        steps = 3,
        modifier = Modifier.padding(vertical = 8.dp),
    )
    Text(
        text = stringResource(R.string.radius_description, radius.roundToInt().toString()),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 16.dp),
    )
    ContinueButton(onClick = onConfirm)
}

@Composable
private fun MessageStep(
    message: String,
    onMessageChanged: (String) -> Unit,
    onSubmit: () -> Unit,
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
    ContinueButton(onClick = {
        if (message.isBlank()) {
            showError = true
        } else {
            onSubmit()
        }
    })
}

@Composable
private fun ContinueButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.continue_description))
    }
}
