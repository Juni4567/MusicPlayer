package remix.myplayer.ui.screen.playing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
  val speed: Float = 1.0f,
  val pitch: Float = 1.0f,
  val echoEnabled: Boolean = false,
  val echoPreset: Int = 0,
)

val NormalMoodPreset = MoodPreset(
  nameRes = R.string.mood_reset,
  icon = "↺",
  speed = 1.00f,
  pitch = 1.00f,
  echoEnabled = false
)

val OtherMoodPresets = listOf(
  MoodPreset(
    nameRes = R.string.mood_slowed_reverb,
    icon = "🌙",
    speed = 0.85f,
    pitch = 0.90f,
    echoEnabled = true,
    echoPreset = 3 /* Plateau */
  ),
  MoodPreset(
    nameRes = R.string.mood_nightcore,
    icon = "⚡",
    speed = 1.20f,
    pitch = 1.15f,
    echoEnabled = false
  ),
  MoodPreset(
    nameRes = R.string.mood_vaporwave,
    icon = "📼",
    speed = 0.82f,
    pitch = 0.84f,
    echoEnabled = true,
    echoPreset = 5 /* Plate */
  ),
  MoodPreset(
    nameRes = R.string.mood_cardio,
    icon = "🏃",
    speed = 1.12f,
    pitch = 1.00f,
    echoEnabled = false
  ),
  MoodPreset(
    nameRes = R.string.mood_lofi,
    icon = "🎧",
    speed = 0.92f,
    pitch = 0.96f,
    echoEnabled = false
  ),
  MoodPreset(
    nameRes = R.string.mood_warm,
    icon = "☕",
    speed = 0.96f,
    pitch = 1.00f,
    echoEnabled = false
  ),
  MoodPreset(
    nameRes = R.string.mood_dreamy,
    icon = "✨",
    speed = 0.90f,
    pitch = 0.95f,
    echoEnabled = true,
    echoPreset = 4 /* Large Room */
  ),
  MoodPreset(
    nameRes = R.string.mood_deep,
    icon = "🌊",
    speed = 0.94f,
    pitch = 0.88f,
    echoEnabled = false
  ),
  MoodPreset(
    nameRes = R.string.mood_turbo,
    icon = "🚀",
    speed = 1.15f,
    pitch = 1.05f,
    echoEnabled = false
  ),
  MoodPreset(
    nameRes = R.string.mood_spatial,
    icon = "🌌",
    speed = 1.00f,
    pitch = 1.00f,
    echoEnabled = true,
    echoPreset = 1 /* Small Room */
  ),
)

@Composable
internal fun PlayingMoodPresets(
  @Suppress("UNUSED_PARAMETER") swatch: Palette.Swatch,
  modifier: Modifier = Modifier
) {
  val settingVM = settingViewModel
  val settingState by settingVM.settingsState.collectAsStateWithLifecycle()
  val theme = LocalTheme.current
  val accent = theme.secondary

  val currentSpeed = settingState.play.speed.toFloatOrNull() ?: 1.0f
  val currentPitch = settingState.play.pitch.toFloatOrNull() ?: 1.0f
  val echoEnabled = settingState.play.enableEcho

  fun applyPreset(preset: MoodPreset) {
    MusicServiceRemote.service?.playback?.speed = preset.speed
    MusicServiceRemote.service?.playback?.pitch = preset.pitch
    settingVM.setSpeed(preset.speed.toString())
    settingVM.setPitch(preset.pitch.toString())
    settingVM.setEchoEnabled(preset.echoEnabled)
    if (preset.echoEnabled) {
      settingVM.setEchoPreset(preset.echoPreset)
    }
  }

  LazyHorizontalGrid(
    rows = GridCells.Fixed(2),
    modifier = modifier
      .fillMaxWidth()
      .height(72.dp),
    horizontalArrangement = Arrangement.spacedBy(7.dp),
    verticalArrangement = Arrangement.spacedBy(7.dp),
    contentPadding = PaddingValues(horizontal = 16.dp)
  ) {
    items(OtherMoodPresets, key = { it.nameRes }) { preset ->
      val isSelected = abs(currentSpeed - preset.speed) < 0.02f &&
        abs(currentPitch - preset.pitch) < 0.02f &&
        (preset.echoEnabled == echoEnabled)

      val chipBg = if (isSelected) accent.copy(alpha = 0.22f) else theme.surface2
      val chipBorder = if (isSelected) accent else theme.divider.copy(alpha = 0.45f)
      val textColor = if (isSelected) accent else theme.textSecondary

      val label = "${preset.icon} ${stringResource(preset.nameRes)}"

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .height(32.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(chipBg)
          .border(0.9.dp, chipBorder, RoundedCornerShape(16.dp))
          .semantics { contentDescription = label }
          .clickWithRipple(circle = false) {
            if (isSelected) {
              applyPreset(NormalMoodPreset)
            } else {
              applyPreset(preset)
            }
          }
          .padding(horizontal = 12.dp)
      ) {
        Text(
          text = label,
          color = textColor,
          fontSize = 12.5.sp,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
      }
    }
  }
}
