package remix.myplayer.ui.screen.playing

import android.media.AudioManager
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
import remix.myplayer.App
import remix.myplayer.R
import remix.myplayer.helper.EQHelper
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
  val eqEnabled: Boolean = false,
  val eqGains: List<Float>? = null,
  val bassBoost: Int = 0,
  val virtualizer: Int = 0,
)

val NormalMoodPreset = MoodPreset(
  nameRes = R.string.mood_reset,
  icon = "↺",
  speed = 1.00f,
  pitch = 1.00f,
  echoEnabled = false,
  eqEnabled = false
)

val OtherMoodPresets = listOf(
  MoodPreset(
    nameRes = R.string.mood_bass_slam,
    icon = "💥",
    speed = 1.00f,
    pitch = 1.00f,
    eqEnabled = true,
    eqGains = listOf(0.70f, 0.45f, -0.05f, 0.10f, 0.25f),
    bassBoost = 800,
    virtualizer = 200
  ),
  MoodPreset(
    nameRes = R.string.mood_spatial,
    icon = "🌌",
    speed = 1.00f,
    pitch = 1.00f,
    echoEnabled = true,
    echoPreset = 1,
    eqEnabled = true,
    eqGains = listOf(0.20f, 0.10f, 0.15f, 0.35f, 0.50f),
    bassBoost = 300,
    virtualizer = 850
  ),
  MoodPreset(
    nameRes = R.string.mood_vocal,
    icon = "🎤",
    speed = 1.00f,
    pitch = 1.00f,
    eqEnabled = true,
    eqGains = listOf(-0.15f, -0.10f, 0.45f, 0.50f, 0.30f),
    bassBoost = 0,
    virtualizer = 250
  ),
  MoodPreset(
    nameRes = R.string.mood_slowed_reverb,
    icon = "🌙",
    speed = 0.85f,
    pitch = 0.90f,
    echoEnabled = true,
    echoPreset = 3 /* Plateau */,
    eqEnabled = true,
    eqGains = listOf(0.50f, 0.30f, -0.10f, 0.15f, 0.20f),
    bassBoost = 600,
    virtualizer = 500
  ),
  MoodPreset(
    nameRes = R.string.mood_nightcore,
    icon = "⚡",
    speed = 1.20f,
    pitch = 1.15f,
    eqEnabled = true,
    eqGains = listOf(0.30f, 0.20f, 0.05f, 0.40f, 0.60f),
    bassBoost = 400,
    virtualizer = 300
  ),
  MoodPreset(
    nameRes = R.string.mood_lofi,
    icon = "🎧",
    speed = 0.92f,
    pitch = 0.98f,
    eqEnabled = true,
    eqGains = listOf(0.40f, 0.30f, 0.10f, -0.20f, -0.35f),
    bassBoost = 350,
    virtualizer = 150
  ),
  MoodPreset(
    nameRes = R.string.mood_vaporwave,
    icon = "📼",
    speed = 0.82f,
    pitch = 0.84f,
    echoEnabled = true,
    echoPreset = 5 /* Plate */,
    eqEnabled = true,
    eqGains = listOf(0.35f, 0.20f, 0.00f, 0.10f, -0.10f),
    bassBoost = 450,
    virtualizer = 400
  ),
  MoodPreset(
    nameRes = R.string.mood_turbo,
    icon = "🚀",
    speed = 1.08f,
    pitch = 1.02f,
    eqEnabled = true,
    eqGains = listOf(0.65f, 0.40f, -0.15f, 0.30f, 0.55f),
    bassBoost = 750,
    virtualizer = 450
  ),
  MoodPreset(
    nameRes = R.string.mood_warm,
    icon = "☕",
    speed = 0.98f,
    pitch = 1.00f,
    eqEnabled = true,
    eqGains = listOf(0.25f, 0.35f, 0.25f, 0.10f, 0.05f),
    bassBoost = 200,
    virtualizer = 150
  ),
  MoodPreset(
    nameRes = R.string.mood_dreamy,
    icon = "✨",
    speed = 0.90f,
    pitch = 0.95f,
    echoEnabled = true,
    echoPreset = 4,
    eqEnabled = true,
    eqGains = listOf(0.15f, 0.05f, 0.10f, 0.45f, 0.65f),
    bassBoost = 200,
    virtualizer = 700
  ),
  MoodPreset(
    nameRes = R.string.mood_deep,
    icon = "🌊",
    speed = 0.94f,
    pitch = 0.90f,
    eqEnabled = true,
    eqGains = listOf(0.75f, 0.40f, -0.10f, 0.00f, 0.10f),
    bassBoost = 850,
    virtualizer = 350
  ),
  MoodPreset(
    nameRes = R.string.mood_cardio,
    icon = "🏃",
    speed = 1.12f,
    pitch = 1.00f,
    eqEnabled = true,
    eqGains = listOf(0.55f, 0.50f, 0.00f, 0.20f, 0.35f),
    bassBoost = 600,
    virtualizer = 200
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

    val sessionId = MusicServiceRemote.service?.playback?.audioSessionId
    if (sessionId != null && sessionId != AudioManager.AUDIO_SESSION_ID_GENERATE) {
      EQHelper.init(App.context, sessionId, force = true)
    }

    if (preset.eqEnabled && preset.eqGains != null) {
      EQHelper.updateEnable(true)
      EQHelper.applyRelativeGains(preset.eqGains.toFloatArray())
      EQHelper.bassBoostStrength = preset.bassBoost
      EQHelper.virtualizerStrength = preset.virtualizer
    } else {
      EQHelper.reset()
      EQHelper.updateEnable(false)
    }
  }

  val isNormalSelected = abs(currentSpeed - NormalMoodPreset.speed) < 0.02f &&
    abs(currentPitch - NormalMoodPreset.pitch) < 0.02f &&
    !echoEnabled && !EQHelper.enable

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
          (!preset.echoEnabled || echoEnabled) &&
          (preset.eqEnabled == EQHelper.enable)

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
