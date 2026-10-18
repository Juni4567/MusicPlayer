package remix.myplayer.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import remix.myplayer.R
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.settingViewModel
import remix.myplayer.viewmodel.settings.SettingsState
import java.util.Locale
import kotlin.math.abs

/** Mi Music playback speeds. */
val PlaybackSpeedOptions: List<Float> = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

fun formatPlaybackSpeed(speed: Float): String = when {
  abs(speed - 0.5f) < 0.01f -> "0.5x"
  abs(speed - 0.75f) < 0.01f -> "0.75x"
  abs(speed - 1.0f) < 0.01f -> "1x"
  abs(speed - 1.25f) < 0.01f -> "1.25x"
  abs(speed - 1.5f) < 0.01f -> "1.5x"
  abs(speed - 2.0f) < 0.01f -> "2x"
  else -> String.format(Locale.US, "%.2fx", speed)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeedDialog(state: DialogState, settingState: SettingsState) {
  val settingVM = settingViewModel
  val current = settingState.play.speed.toFloatOrNull() ?: 1.0f
  val theme = LocalTheme.current
  val accent = theme.secondary
  val onAccent = theme.primaryReverse
  val textPrimary = theme.textPrimary
  val surface2 = theme.surface2
  val divider = theme.divider

  MiBottomSheet(
    visible = state.isOpen,
    onDismiss = { state.dismiss() },
    title = stringResource(R.string.speed),
  ) {
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      PlaybackSpeedOptions.forEach { speed ->
        val selected = abs(speed - current) < 0.01f
        val label = formatPlaybackSpeed(speed)
        Text(
          text = label,
          color = if (selected) onAccent else textPrimary,
          fontSize = 15.sp,
          fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) accent else surface2)
            .border(
              width = 1.dp,
              color = if (selected) Color.Transparent else divider,
              shape = RoundedCornerShape(24.dp)
            )
            .semantics { contentDescription = "Playback speed $label" }
            .clickWithRipple(circle = false) {
              settingVM.setSpeed(speedPreferenceValue(speed))
              state.dismiss()
            }
            .padding(horizontal = 18.dp, vertical = 12.dp)
        )
      }
    }
  }
}
