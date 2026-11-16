package remix.myplayer.ui.screen.playing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import remix.myplayer.R
import remix.myplayer.service.MusicServiceRemote
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.settingViewModel
import kotlin.math.abs

data class MoodPreset(
  val nameRes: Int,
  val icon: String,
  val speed: Float,
  val pitch: Float,
  val echoEnabled: Boolean = false,
  val echoPreset: Int = 0,
)

val MoodPresetsList = listOf(
  MoodPreset(R.string.mood_slowed_reverb, "🌙", 0.85f, 0.92f, echoEnabled = true, echoPreset = 3 /* Plateau */),
  MoodPreset(R.string.mood_nightcore, "⚡", 1.20f, 1.15f, echoEnabled = false),
  MoodPreset(R.string.mood_vaporwave, "📼", 0.80f, 0.85f, echoEnabled = true, echoPreset = 5 /* Plate */),
  MoodPreset(R.string.mood_cardio, "🏃", 1.10f, 1.00f, echoEnabled = false),
  MoodPreset(R.string.mood_lofi, "🎧", 0.90f, 1.00f, echoEnabled = false),
  MoodPreset(R.string.mood_reset, "↺", 1.00f, 1.00f, echoEnabled = false),
)

@Composable
internal fun PlayingMoodPresets(
  swatch: Palette.Swatch,
  modifier: Modifier = Modifier
) {
  val settingVM = settingViewModel
  val settingState by settingVM.settingsState.collectAsStateWithLifecycle()
  val theme = LocalTheme.current
  val accent = Color(swatch.rgb)

  val currentSpeed = settingState.play.speed.toFloatOrNull() ?: 1.0f
  val currentPitch = settingState.play.pitch.toFloatOrNull() ?: 1.0f
  val echoEnabled = settingState.play.enableEcho

  LazyRow(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    items(MoodPresetsList, key = { it.nameRes }) { preset ->
      val isSelected = abs(currentSpeed - preset.speed) < 0.02f &&
        abs(currentPitch - preset.pitch) < 0.02f &&
        (!preset.echoEnabled || echoEnabled)

      val chipBg = if (isSelected) accent.copy(alpha = 0.22f) else theme.surface2
      val chipBorder = if (isSelected) accent else theme.divider.copy(alpha = 0.45f)
      val textColor = if (isSelected) accent else theme.textSecondary

      val label = "${preset.icon} ${stringResource(preset.nameRes)}"

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .height(30.dp)
          .clip(RoundedCornerShape(15.dp))
          .background(chipBg)
          .border(0.9.dp, chipBorder, RoundedCornerShape(15.dp))
          .semantics { contentDescription = label }
          .clickWithRipple(circle = false) {
            MusicServiceRemote.service?.playback?.speed = preset.speed
            MusicServiceRemote.service?.playback?.pitch = preset.pitch
            settingVM.setSpeed(preset.speed.toString())
            settingVM.setPitch(preset.pitch.toString())
            settingVM.setEchoEnabled(preset.echoEnabled)
            if (preset.echoEnabled) {
              settingVM.setEchoPreset(preset.echoPreset)
            }
          }
          .padding(horizontal = 10.dp)
      ) {
        Text(
          text = label,
          color = textColor,
          fontSize = 11.5.sp,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
      }
    }
  }
}
