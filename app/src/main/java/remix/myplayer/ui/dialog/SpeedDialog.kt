package remix.myplayer.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import remix.myplayer.R
import remix.myplayer.viewmodel.settingViewModel
import remix.myplayer.viewmodel.settings.SettingsState
import java.util.Locale
import kotlin.math.abs

/** Playback speed presets from -2.0x to 2.0x in 0.5 steps (0 excluded). */
val PlaybackSpeedOptions: List<Float> = buildList {
  var speed = -2.0f
  while (speed <= 2.0f + 0.001f) {
    if (abs(speed) > 0.001f) {
      add(speed)
    }
    speed += 0.5f
  }
}

fun formatPlaybackSpeed(speed: Float): String {
  return String.format(Locale.US, "%.1fx", speed)
}

@Composable
fun SpeedDialog(state: DialogState, settingState: SettingsState) {
  val settingVM = settingViewModel
  val current = settingState.play.speed.toFloatOrNull() ?: 1.0f
  val selectedIndex = remember(current) {
    PlaybackSpeedOptions.indexOfFirst { abs(it - current) < 0.01f }
      .takeIf { it >= 0 }
      ?: PlaybackSpeedOptions.indexOfFirst { abs(it - 1.0f) < 0.01f }.coerceAtLeast(0)
  }

  NormalDialog(
    dialogState = state,
    title = stringResource(R.string.speed),
    items = PlaybackSpeedOptions.map { formatPlaybackSpeed(it) },
    positive = null,
    negative = stringResource(R.string.cancel),
    itemsCallbackSingleChoice = ItemsCallbackSingleChoice(selectedIndex) { index ->
      val speed = PlaybackSpeedOptions.getOrNull(index) ?: return@ItemsCallbackSingleChoice
      settingVM.setSpeed(String.format(Locale.US, "%.1f", speed))
      state.dismiss()
    }
  )
}
