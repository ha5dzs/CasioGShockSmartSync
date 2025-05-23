package org.avmedia.gshockGoogleSync.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import org.avmedia.gshockGoogleSync.R

@Composable
fun OperationalTone(
    onUpdate: (SettingsViewModel.OperationSound) -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val classType = SettingsViewModel.OperationSound::class.java

    val settings by settingsViewModel.settings.collectAsState()
    val operationToneSetting: SettingsViewModel.OperationSound =
        settingsViewModel.getSetting(classType)
    var sound by remember { mutableStateOf(operationToneSetting.sound) }

    LaunchedEffect(settings) {
        sound = operationToneSetting.sound
    }

    val title = settingsViewModel.translateApi.stringResource(
        context = LocalContext.current,
        id = R.string.operational_sound
    )
    BasicSettings(
        title = title,
        isSwitchOn = sound,
        onSwitchToggle = { newValue ->
            sound = newValue // Update the state when the switch is toggled
            operationToneSetting.sound = newValue
            onUpdate(operationToneSetting.copy(sound = newValue))
        },
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewOperationalTone() {
    OperationalTone(onUpdate = {})
}