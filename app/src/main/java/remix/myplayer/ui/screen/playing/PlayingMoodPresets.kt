package remix.myplayer.ui.screen.playing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
  val speed: Float,
  val pitch: Float,
  val echoEnabled: Boolean = false,
  val echoPreset: Int = 0,
)

val NormalMoodPreset = MoodPreset(
  R.string.mood_reset,
  "↺",
  1.00f,
  1.00f,
  echoEnabled = false
)

val OtherMoodPresets = listOf(
  MoodPreset(R.string.mood_slowed_reverb, "🌙", 0.85f, 0.92f, echoEnabled = true, echoPreset = 3 /* Plateau */),
  MoodPreset(R.string.mood_nightcore, "⚡", 1.20f, 1.15f, echoEnabled = false),
  MoodPreset(R.string.mood_vaporwave, "📼", 0.80f, 0.85f, echoEnabled = true, echoPreset = 5 /* Plate */),
  MoodPreset(R.string.mood_cardio, "🏃", 1.10f, 1.00f, echoEnabled = false),
  MoodPreset(R.string.mood_lofi, "🎧", 0.90f, 1.00f, echoEnabled = false),
  MoodPreset(R.string.mood_warm, "☕", 0.95f, 0.98f, echoEnabled = true, echoPreset = 1),
  MoodPreset(R.string.mood_dreamy, "✨", 0.88f, 0.94f, echoEnabled = true, echoPreset = 4),
  MoodPreset(R.string.mood_deep, "🌌", 0.92f, 0.88f, echoEnabled = false),
  MoodPreset(R.string.mood_turbo, "🚀", 1.25f, 1.05f, echoEnabled = false),
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

  val isNormalSelected = abs(currentSpeed - NormalMoodPreset.speed) < 0.02f &&
    abs(currentPitch - NormalMoodPreset.pitch) < 0.02f &&
    !echoEnabled

  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Pinned Standard / Normal preset on the left
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier
        .padding(start = 16.dp, end = 6.dp)
        .height(60.dp)
        .width(66.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(if (isNormalSelected) accent.copy(alpha = 0.22f) else theme.surface2)
        .border(
          0.9.dp,
          if (isNormalSelected) accent else theme.divider.copy(alpha = 0.45f),
          RoundedCornerShape(12.dp)
        )
        .semantics { contentDescription = "Normal playback preset" }
        .clickWithRipple(circle = false) {
          applyPreset(NormalMoodPreset)
        }
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "↺",
          color = if (isNormalSelected) accent else theme.textPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = stringResource(R.string.mood_reset),
          color = if (isNormalSelected) accent else theme.textSecondary,
          fontSize = 11.sp,
          fontWeight = if (isNormalSelected) FontWeight.SemiBold else FontWeight.Medium
        )
      }
    }

    // Two rows of scrollable mood presets
    LazyHorizontalGrid(
      rows = GridCells.Fixed(2),
      modifier = Modifier
        .weight(1f)
        .height(62.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
      contentPadding = PaddingValues(end = 16.dp)
    ) {
      items(OtherMoodPresets, key = { it.nameRes }) { preset ->
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
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(chipBg)
            .border(0.9.dp, chipBorder, RoundedCornerShape(14.dp))
            .semantics { contentDescription = label }
            .clickWithRipple(circle = false) {
              applyPreset(preset)
            }
            .padding(horizontal = 9.dp)
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
}
