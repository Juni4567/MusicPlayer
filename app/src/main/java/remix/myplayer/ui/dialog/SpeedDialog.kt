package remix.myplayer.ui.dialog

import androidx.compose.runtime.Composable
import remix.myplayer.R
import remix.myplayer.viewmodel.settings.SettingsState
import java.util.Locale
import kotlin.math.abs

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

/**
 * Backward compatibility wrapper routing directly to the unified [PlaybackAndEqSheet].
 */
@Composable
fun SpeedDialog(state: DialogState, settingState: SettingsState) {
  PlaybackAndEqSheet(state = state, settingState = settingState, initialTab = 0)
}
