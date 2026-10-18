package remix.myplayer.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import remix.myplayer.R
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.service.MusicServiceRemote
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.LineSlider
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.ui.widget.common.TextSecondary
import remix.myplayer.ui.widget.common.defaultLineSliderProperties
import remix.myplayer.util.ext.clickWithRipple
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

@OptIn(ExperimentalLayoutApi::class)
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
  val onAccent = theme.primaryReverse
  val textPrimary = theme.textPrimary
  val surface2 = theme.surface2
  val divider = theme.divider
  val volumePercent = (volumeBoost * 100f).roundToInt()

  MiBottomSheet(
    visible = state.isOpen,
    onDismiss = { state.dismiss() },
    title = stringResource(R.string.playback_effects),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 520.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextSecondary(stringResource(R.string.volume_boost), fontSize = 13.sp)
        TextPrimary("$volumePercent%", fontSize = 13.sp)
      }
      LineSlider(
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp)
          .padding(bottom = 4.dp)
          .semantics { contentDescription = "Volume boost $volumePercent percent" },
        value = volumeBoost,
        onValueChange = { v ->
          val normalized = SettingPrefs.normalizeVolumeBoost(v)
          volumeBoost = normalized
          // Live preview via DSP gain (Media3 clamps Player.setVolume to 1.0).
          MusicServiceRemote.service
            ?.replayGainController
            ?.processor
            ?.setVolumeBoost(normalized)
        },
        onValueChangeFinished = {
          settingVM.setVolumeBoost(volumeBoost)
        },
        steps = ((SettingPrefs.VOLUME_BOOST_MAX - SettingPrefs.VOLUME_BOOST_MIN) /
          SettingPrefs.VOLUME_BOOST_STEP).roundToInt() - 1,
        valueRange = SettingPrefs.VOLUME_BOOST_MIN..SettingPrefs.VOLUME_BOOST_MAX,
        properties = defaultLineSliderProperties.copy(
          trackHeight = 6.dp,
          thumbWidth = 16.dp,
          thumbHeight = 16.dp,
        ),
      )
      Text(
        text = stringResource(R.string.volume_boost_hint),
        color = theme.textMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 16.dp)
      )

      TextSecondary(
        stringResource(R.string.speed),
        fontSize = 13.sp,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      FlowRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        PlaybackSpeedOptions.forEach { speed ->
          val selected = abs(speed - currentSpeed) < 0.01f
          val label = formatPlaybackSpeed(speed)
          OptionChip(
            label = label,
            selected = selected,
            contentDescription = "Playback speed $label",
            accent = accent,
            onAccent = onAccent,
            textPrimary = textPrimary,
            surface2 = surface2,
            divider = divider,
            onClick = { settingVM.setSpeed(speedPreferenceValue(speed)) }
          )
        }
      }

      TextSecondary(
        stringResource(R.string.pitch),
        fontSize = 13.sp,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      FlowRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        PlaybackPitchOptions.forEach { pitch ->
          val selected = abs(pitch - currentPitch) < 0.01f
          val label = formatPlaybackPitch(pitch)
          OptionChip(
            label = label,
            selected = selected,
            contentDescription = "Pitch $label",
            accent = accent,
            onAccent = onAccent,
            textPrimary = textPrimary,
            surface2 = surface2,
            divider = divider,
            onClick = { settingVM.setPitch(pitchPreferenceValue(pitch)) }
          )
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextSecondary(stringResource(R.string.echo), fontSize = 13.sp)
        Switch(
          modifier = Modifier
            .scale(0.85f)
            .semantics { contentDescription = "Enable echo" },
          checked = echoEnabled,
          colors = SwitchDefaults.colors().copy(
            checkedTrackColor = accent,
            uncheckedTrackColor = Color.Transparent
          ),
          onCheckedChange = { checked ->
            settingVM.setEchoEnabled(checked)
            if (checked && echoPreset == 0) {
              settingVM.setEchoPreset(1)
            }
          }
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        EchoPresetNameRes.forEachIndexed { index, nameRes ->
          if (index == 0) return@forEachIndexed
          val selected = echoEnabled && echoPreset == index
          OptionChip(
            label = stringResource(nameRes),
            selected = selected,
            contentDescription = "Echo ${stringResource(nameRes)}",
            accent = accent,
            onAccent = onAccent,
            textPrimary = textPrimary,
            surface2 = surface2,
            divider = divider,
            enabled = echoEnabled,
            onClick = { settingVM.setEchoPreset(index) }
          )
        }
      }
      Text(
        text = stringResource(R.string.echo_hint),
        color = theme.textMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 8.dp)
      )
    }
  }
}

@Composable
private fun OptionChip(
  label: String,
  selected: Boolean,
  contentDescription: String,
  accent: Color,
  onAccent: Color,
  textPrimary: Color,
  surface2: Color,
  divider: Color,
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  Text(
    text = label,
    color = when {
      !enabled -> textPrimary.copy(alpha = 0.35f)
      selected -> onAccent
      else -> textPrimary
    },
    fontSize = 15.sp,
    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .heightIn(min = 48.dp)
      .clip(RoundedCornerShape(24.dp))
      .background(
        when {
          !enabled -> surface2.copy(alpha = 0.5f)
          selected -> accent
          else -> surface2
        }
      )
      .border(
        width = 1.dp,
        color = if (selected || !enabled) Color.Transparent else divider,
        shape = RoundedCornerShape(24.dp)
      )
      .semantics { this.contentDescription = contentDescription }
      .clickWithRipple(circle = false, enabled = enabled) { onClick() }
      .padding(horizontal = 18.dp, vertical = 12.dp)
  )
}
