package remix.myplayer.ui.dialog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import remix.myplayer.R
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.service.MusicServiceRemote
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.viewmodel.settingViewModel
import remix.myplayer.viewmodel.settings.SettingsState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/** Mi Music playback speeds. */
val PlaybackSpeedOptions: List<Float> = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

/** Pitch ratios (1.0 = original). */
val PlaybackPitchOptions: List<Float> = listOf(0.75f, 0.85f, 1.0f, 1.15f, 1.25f, 1.5f)

/** Echo preset label resources; index 0 = Off / NONE. */
val EchoPresetNameRes: List<Int> = listOf(
  R.string.echo_off,
  R.string.echo_small,
  R.string.echo_medium,
  R.string.echo_large,
  R.string.echo_hall,
  R.string.echo_hall_large,
  R.string.echo_plate,
)

fun formatPlaybackSpeed(speed: Float): String = when {
  abs(speed - 0.5f) < 0.01f -> "0.5x"
  abs(speed - 0.75f) < 0.01f -> "0.75x"
  abs(speed - 1.0f) < 0.01f -> "1x"
  abs(speed - 1.25f) < 0.01f -> "1.25x"
  abs(speed - 1.5f) < 0.01f -> "1.5x"
  abs(speed - 2.0f) < 0.01f -> "2x"
  else -> String.format(Locale.US, "%.2fx", speed)
}

fun formatPlaybackPitch(pitch: Float): String = when {
  abs(pitch - 0.75f) < 0.01f -> "0.75"
  abs(pitch - 0.85f) < 0.01f -> "0.85"
  abs(pitch - 1.0f) < 0.01f -> "1.0"
  abs(pitch - 1.15f) < 0.01f -> "1.15"
  abs(pitch - 1.25f) < 0.01f -> "1.25"
  abs(pitch - 1.5f) < 0.01f -> "1.5"
  else -> String.format(Locale.US, "%.2f", pitch)
}

fun speedPreferenceValue(speed: Float): String = when {
  abs(speed - 0.5f) < 0.01f -> "0.5"
  abs(speed - 0.75f) < 0.01f -> "0.75"
  abs(speed - 1.0f) < 0.01f -> "1.0"
  abs(speed - 1.25f) < 0.01f -> "1.25"
  abs(speed - 1.5f) < 0.01f -> "1.5"
  abs(speed - 2.0f) < 0.01f -> "2.0"
  else -> String.format(Locale.US, "%.2f", speed)
}

fun pitchPreferenceValue(pitch: Float): String = formatPlaybackPitch(pitch)

@Composable
fun SpeedDialog(state: DialogState, settingState: SettingsState) {
  val settingVM = settingViewModel
  val currentSpeed = settingState.play.speed.toFloatOrNull() ?: 1.0f
  val currentPitch = settingState.play.pitch.toFloatOrNull() ?: 1.0f
  val echoEnabled = settingState.play.enableEcho
  val echoPreset = settingState.play.echoPreset.coerceIn(0, EchoPresetNameRes.lastIndex)
  var volumeBoost by remember(settingState.play.volumeBoost) {
    mutableFloatStateOf(settingState.play.volumeBoost)
  }
  val theme = LocalTheme.current
  val accent = theme.secondary
  val volumePercent = (volumeBoost * 100f).roundToInt()

  MiBottomSheet(
    visible = state.isOpen,
    onDismiss = { state.dismiss() },
    title = stringResource(R.string.playback_effects),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 440.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      SheetSliderRow(
        label = stringResource(R.string.volume_boost),
        valueLabel = "$volumePercent%",
        value = volumeBoost,
        valueRange = SettingPrefs.VOLUME_BOOST_MIN..SettingPrefs.VOLUME_BOOST_MAX,
        steps = ((SettingPrefs.VOLUME_BOOST_MAX - SettingPrefs.VOLUME_BOOST_MIN) /
          SettingPrefs.VOLUME_BOOST_STEP).roundToInt() - 1,
        contentDescription = "Volume boost $volumePercent percent",
        onValueChange = { v ->
          val normalized = SettingPrefs.normalizeVolumeBoost(v)
          volumeBoost = normalized
          MusicServiceRemote.service
            ?.replayGainController
            ?.processor
            ?.setVolumeBoost(normalized)
        },
        onValueChangeFinished = { settingVM.setVolumeBoost(volumeBoost) },
      )

      Column {
        SheetSectionLabel(stringResource(R.string.speed))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          PlaybackSpeedOptions.forEach { speed ->
            val selected = abs(speed - currentSpeed) < 0.01f
            val label = formatPlaybackSpeed(speed)
            SheetChip(
              label = label,
              selected = selected,
              contentDescription = "Playback speed $label",
              onClick = { settingVM.setSpeed(speedPreferenceValue(speed)) },
            )
          }
        }
      }

      Column {
        SheetSectionLabel(stringResource(R.string.pitch))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          PlaybackPitchOptions.forEach { pitch ->
            val selected = abs(pitch - currentPitch) < 0.01f
            val label = formatPlaybackPitch(pitch)
            SheetChip(
              label = label,
              selected = selected,
              contentDescription = "Pitch $label",
              onClick = { settingVM.setPitch(pitchPreferenceValue(pitch)) },
            )
          }
        }
      }

      Column {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.echo),
            color = theme.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
          )
          Switch(
            modifier = Modifier
              .scale(0.78f)
              .semantics { contentDescription = "Enable echo" },
            checked = echoEnabled,
            colors = SwitchDefaults.colors().copy(
              checkedTrackColor = accent,
              uncheckedTrackColor = Color.Transparent,
            ),
            onCheckedChange = { checked ->
              settingVM.setEchoEnabled(checked)
              if (checked && echoPreset == 0) {
                settingVM.setEchoPreset(1)
              }
            },
          )
        }
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          EchoPresetNameRes.forEachIndexed { index, nameRes ->
            if (index == 0) return@forEachIndexed
            val selected = echoEnabled && echoPreset == index
            SheetChip(
              label = stringResource(nameRes),
              selected = selected,
              enabled = echoEnabled,
              contentDescription = "Echo ${stringResource(nameRes)}",
              onClick = { settingVM.setEchoPreset(index) },
            )
          }
        }
      }
    }
  }
}
