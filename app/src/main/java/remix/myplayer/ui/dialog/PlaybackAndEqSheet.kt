package remix.myplayer.ui.dialog

import android.media.audiofx.AudioEffect
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import remix.myplayer.R
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.helper.EQHelper
import remix.myplayer.service.MusicServiceRemote
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.LineSlider
import remix.myplayer.ui.widget.common.defaultLineSliderProperties
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.settingViewModel
import remix.myplayer.viewmodel.settings.SettingsState
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

private class EqPresetItem(val nameRes: Int, val gains: FloatArray)

private val PresetsList = listOf(
  EqPresetItem(R.string.eq_preset_flat, floatArrayOf(0f, 0f, 0f, 0f, 0f)),
  EqPresetItem(R.string.eq_preset_rock, floatArrayOf(0.45f, 0.25f, -0.1f, 0.2f, 0.4f)),
  EqPresetItem(R.string.eq_preset_pop, floatArrayOf(-0.15f, 0.25f, 0.35f, 0.2f, -0.1f)),
  EqPresetItem(R.string.eq_preset_classical, floatArrayOf(0.4f, 0.25f, -0.2f, 0.25f, 0.35f)),
  EqPresetItem(R.string.eq_preset_vocal, floatArrayOf(-0.2f, 0.1f, 0.45f, 0.35f, 0.1f)),
  EqPresetItem(R.string.eq_preset_jazz, floatArrayOf(0.3f, 0.1f, -0.15f, 0.15f, 0.35f)),
  EqPresetItem(R.string.eq_preset_dance, floatArrayOf(0.5f, 0.35f, 0.05f, -0.1f, 0.25f)),
  EqPresetItem(R.string.eq_preset_bass, floatArrayOf(0.7f, 0.45f, 0.1f, -0.15f, -0.2f)),
  EqPresetItem(R.string.eq_preset_treble, floatArrayOf(-0.25f, -0.1f, 0.15f, 0.45f, 0.65f)),
)

private val QuickSpeedPresets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
private val QuickPitchPresets = listOf(0.75f, 0.85f, 1.0f, 1.15f, 1.25f, 1.5f)

private const val SPEED_MIN = 0.5f
private const val SPEED_MAX = 2.0f
private const val PITCH_MIN = 0.5f
private const val PITCH_MAX = 1.5f

/**
 * Unified, compact bottom sheet merging Playback and Equalizer settings.
 * Includes continuous sliders with real-time feedback for playback speed and pitch.
 */
@Composable
fun PlaybackAndEqSheet(
  state: DialogState,
  settingState: SettingsState,
  initialTab: Int = 0,
) {
  if (!state.isOpen) return

  val context = LocalContext.current
  val theme = LocalTheme.current
  val accent = theme.secondary
  val settingVM = settingViewModel

  var currentTab by remember(state.isOpen, initialTab) {
    mutableIntStateOf(initialTab.coerceIn(0, 1))
  }

  // Playback state
  val currentSpeed = settingState.play.speed.toFloatOrNull() ?: 1.0f
  var speedSliderValue by remember(currentSpeed) { mutableFloatStateOf(currentSpeed) }

  val currentPitch = settingState.play.pitch.toFloatOrNull() ?: 1.0f
  var pitchSliderValue by remember(currentPitch) { mutableFloatStateOf(currentPitch) }

  var volumeBoost by remember(settingState.play.volumeBoost) {
    mutableFloatStateOf(settingState.play.volumeBoost)
  }
  val echoEnabled = settingState.play.enableEcho
  val echoPreset = settingState.play.echoPreset.coerceIn(0, EchoPresetNameRes.lastIndex)

  // Equalizer state
  val sessionId = MusicServiceRemote.getAudioSessionId()
  val eqReady = remember(sessionId, state.isOpen) {
    if (sessionId != null && sessionId != AudioEffect.ERROR_BAD_VALUE) {
      EQHelper.init(context, sessionId, true)
    } else {
      false
    }
  }

  var enableEq by remember(eqReady) { mutableStateOf(if (eqReady) EQHelper.enable else false) }
  val bandNumber = if (eqReady) EQHelper.bandNumber.toInt().coerceAtLeast(1) else 5
  val displayBands = min(5, bandNumber)
  val minLevel = if (eqReady) EQHelper.minLevel else -1000
  val maxLevel = if (eqReady) EQHelper.maxLevel else 1000
  val levelRange = (maxLevel - minLevel).toFloat().coerceAtLeast(1f)
  val decimalFormat = remember { DecimalFormat("+#;-#") }
  val minLevelText = decimalFormat.format(minLevel / 100)
  val maxLevelText = decimalFormat.format(maxLevel / 100)

  val bandProgress = remember(eqReady, bandNumber, minLevel) {
    mutableStateListOf<Float>().apply {
      repeat(bandNumber) { i ->
        val level = if (eqReady) EQHelper.getBandLevel(i) else 0
        add((level - minLevel).toFloat())
      }
    }
  }

  var bassStrength by remember(eqReady) {
    mutableFloatStateOf(if (eqReady) EQHelper.bassBoostStrength.toFloat() else 0f)
  }
  var virtualizerStrength by remember(eqReady) {
    mutableFloatStateOf(if (eqReady) EQHelper.virtualizerStrength.toFloat() else 0f)
  }
  var selectedPreset by remember { mutableIntStateOf(-1) }

  fun refreshBands() {
    if (!eqReady) return
    repeat(bandNumber) { i ->
      bandProgress[i] = (EQHelper.getBandLevel(i) - minLevel).toFloat()
    }
  }

  fun resetEq() {
    if (!eqReady) return
    EQHelper.reset()
    selectedPreset = 0
    bassStrength = 0f
    virtualizerStrength = 0f
    refreshBands()
  }

  fun resetPlayback() {
    speedSliderValue = 1.0f
    pitchSliderValue = 1.0f
    volumeBoost = 1.0f
    MusicServiceRemote.service?.playback?.speed = 1.0f
    MusicServiceRemote.service?.playback?.pitch = 1.0f
    MusicServiceRemote.service?.replayGainController?.processor?.setVolumeBoost(1.0f)
    settingVM.setSpeed("1.0")
    settingVM.setPitch("1.0")
    settingVM.setVolumeBoost(1.0f)
    settingVM.setEchoEnabled(false)
  }

  MiBottomSheet(
    visible = true,
    onDismiss = { state.dismiss() },
    title = stringResource(R.string.playback_and_eq),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 480.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Compact Segmented Tab Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(theme.surface2)
          .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        val tabModifier = Modifier
          .weight(1f)
          .height(34.dp)
          .clip(RoundedCornerShape(9.dp))

        Box(
          modifier = tabModifier
            .background(if (currentTab == 0) accent else Color.Transparent)
            .clickWithRipple(circle = false) { currentTab = 0 },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = stringResource(R.string.playback_tab),
            color = if (currentTab == 0) theme.primaryReverse else theme.textSecondary,
            fontSize = 13.sp,
            fontWeight = if (currentTab == 0) FontWeight.SemiBold else FontWeight.Medium,
          )
        }

        Box(
          modifier = tabModifier
            .background(if (currentTab == 1) accent else Color.Transparent)
            .clickWithRipple(circle = false) { currentTab = 1 },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = stringResource(R.string.equalizer_tab),
            color = if (currentTab == 1) theme.primaryReverse else theme.textSecondary,
            fontSize = 13.sp,
            fontWeight = if (currentTab == 1) FontWeight.SemiBold else FontWeight.Medium,
          )
        }
      }

      // Content for Selected Tab
      AnimatedContent(
        targetState = currentTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "TabContentTransition",
        modifier = Modifier.weight(1f, fill = false),
      ) { tabIndex ->
        if (tabIndex == 0) {
          // Playback & Speed Tab
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            // Speed Slider & Presets
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = stringResource(R.string.speed),
                  color = theme.textSecondary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = String.format(Locale.US, "%.2fx", speedSliderValue),
                    color = theme.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                  )
                  if (abs(speedSliderValue - 1.0f) > 0.01f) {
                    Text(
                      text = "1.0x",
                      color = accent,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickWithRipple {
                          speedSliderValue = 1.0f
                          MusicServiceRemote.service?.playback?.speed = 1.0f
                          settingVM.setSpeed("1.0")
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                  }
                }
              }

              LineSlider(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(32.dp),
                value = speedSliderValue,
                valueRange = SPEED_MIN..SPEED_MAX,
                steps = 29, // 0.05 step intervals from 0.5 to 2.0
                onValueChange = { v ->
                  val stepped = ((v * 20f).roundToInt() / 20f).coerceIn(SPEED_MIN, SPEED_MAX)
                  speedSliderValue = stepped
                  MusicServiceRemote.service?.playback?.speed = stepped
                },
                onValueChangeFinished = {
                  settingVM.setSpeed(speedPreferenceValue(speedSliderValue))
                },
                properties = defaultLineSliderProperties.copy(
                  trackHeight = 4.dp,
                  thumbWidth = 14.dp,
                  thumbHeight = 14.dp,
                ),
              )

              // Quick Speed Preset Chips
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                QuickSpeedPresets.forEach { presetSpeed ->
                  val selected = abs(presetSpeed - speedSliderValue) < 0.02f
                  SheetChip(
                    label = formatPlaybackSpeed(presetSpeed),
                    selected = selected,
                    contentDescription = "Speed ${formatPlaybackSpeed(presetSpeed)}",
                    onClick = {
                      speedSliderValue = presetSpeed
                      MusicServiceRemote.service?.playback?.speed = presetSpeed
                      settingVM.setSpeed(speedPreferenceValue(presetSpeed))
                    },
                  )
                }
              }
            }

            // Pitch Slider & Presets
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = stringResource(R.string.pitch),
                  color = theme.textSecondary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = String.format(Locale.US, "%.2f", pitchSliderValue),
                    color = theme.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                  )
                  if (abs(pitchSliderValue - 1.0f) > 0.01f) {
                    Text(
                      text = "1.0",
                      color = accent,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickWithRipple {
                          pitchSliderValue = 1.0f
                          MusicServiceRemote.service?.playback?.pitch = 1.0f
                          settingVM.setPitch("1.0")
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                  }
                }
              }

              LineSlider(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(32.dp),
                value = pitchSliderValue,
                valueRange = PITCH_MIN..PITCH_MAX,
                steps = 19, // 0.05 step intervals from 0.5 to 1.5
                onValueChange = { v ->
                  val stepped = ((v * 20f).roundToInt() / 20f).coerceIn(PITCH_MIN, PITCH_MAX)
                  pitchSliderValue = stepped
                  MusicServiceRemote.service?.playback?.pitch = stepped
                },
                onValueChangeFinished = {
                  settingVM.setPitch(pitchPreferenceValue(pitchSliderValue))
                },
                properties = defaultLineSliderProperties.copy(
                  trackHeight = 4.dp,
                  thumbWidth = 14.dp,
                  thumbHeight = 14.dp,
                ),
              )

              // Quick Pitch Preset Chips
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                QuickPitchPresets.forEach { presetPitch ->
                  val selected = abs(presetPitch - pitchSliderValue) < 0.02f
                  SheetChip(
                    label = formatPlaybackPitch(presetPitch),
                    selected = selected,
                    contentDescription = "Pitch ${formatPlaybackPitch(presetPitch)}",
                    onClick = {
                      pitchSliderValue = presetPitch
                      MusicServiceRemote.service?.playback?.pitch = presetPitch
                      settingVM.setPitch(pitchPreferenceValue(presetPitch))
                    },
                  )
                }
              }
            }

            // Volume Boost Slider
            val volumePercent = (volumeBoost * 100f).roundToInt()
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

            // Echo Effect Section
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

            // Reset Playback Controls Button
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = stringResource(R.string.reset_playback),
                color = theme.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .border(1.dp, theme.divider, RoundedCornerShape(8.dp))
                  .clickWithRipple { resetPlayback() }
                  .padding(horizontal = 14.dp, vertical = 6.dp),
              )
            }
          }
        } else {
          // Equalizer Tab
          if (!eqReady) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(
                text = stringResource(R.string.no_equalizer),
                color = theme.textMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
              )
            }
          } else {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              // Master EQ Toggle & Reset
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = stringResource(R.string.use_eq),
                  color = theme.textPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium,
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = stringResource(R.string.reset_default),
                    color = accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                      .semantics { contentDescription = "Reset equalizer" }
                      .clickWithRipple(circle = false) { resetEq() }
                      .padding(horizontal = 8.dp, vertical = 4.dp),
                  )
                  Switch(
                    modifier = Modifier
                      .scale(0.78f)
                      .semantics { contentDescription = "Enable equalizer" },
                    checked = enableEq,
                    colors = SwitchDefaults.colors().copy(
                      checkedTrackColor = accent,
                      uncheckedTrackColor = Color.Transparent,
                    ),
                    onCheckedChange = { checked ->
                      enableEq = checked
                      EQHelper.updateEnable(checked)
                      if (!EQHelper.isBassBoostEnabled) {
                        bassStrength = EQHelper.bassBoostStrength.toFloat()
                      }
                    },
                  )
                }
              }

              // Presets Carousel
              Column {
                SheetSectionLabel(stringResource(R.string.eq_presets))
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  PresetsList.forEachIndexed { index, preset ->
                    SheetChip(
                      label = stringResource(preset.nameRes),
                      selected = selectedPreset == index,
                      enabled = enableEq,
                      contentDescription = stringResource(preset.nameRes),
                      onClick = {
                        EQHelper.applyRelativeGains(preset.gains)
                        selectedPreset = index
                        refreshBands()
                      },
                    )
                  }
                }
              }

              // Bass Boost
              SheetSliderRow(
                label = stringResource(R.string.bass_boost),
                valueLabel = "${(bassStrength / 10f).roundToInt()}%",
                value = bassStrength,
                valueRange = 0f..1000f,
                enabled = enableEq && EQHelper.isBassBoostEnabled,
                onValueChange = { v ->
                  bassStrength = v
                  if (EQHelper.isBassBoostEnabled) {
                    EQHelper.bassBoostStrength = v.roundToInt()
                  }
                },
              )

              // Virtualizer / Surround
              if (EQHelper.isVirtualizerSupported) {
                SheetSliderRow(
                  label = stringResource(R.string.virtualizer),
                  valueLabel = "${(virtualizerStrength / 10f).roundToInt()}%",
                  value = virtualizerStrength,
                  valueRange = 0f..1000f,
                  enabled = enableEq && EQHelper.isVirtualizerEnabled,
                  onValueChange = { v ->
                    virtualizerStrength = v
                    EQHelper.virtualizerStrength = v.roundToInt()
                  },
                )
              }

              // 5-Band Equalizer
              Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SheetSectionLabel(stringResource(R.string.eq))
                repeat(displayBands) { index ->
                  val progress = bandProgress.getOrElse(index) { 0f }
                  val db = ((progress + minLevel) / 100f).roundToInt()
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Text(
                      text = formatFreq(EQHelper.getCenterFreq(index)),
                      color = theme.textSecondary,
                      fontSize = 11.sp,
                      textAlign = TextAlign.Start,
                      modifier = Modifier.width(52.dp),
                      maxLines = 1,
                    )
                    Text(
                      text = minLevelText,
                      color = theme.textMuted,
                      fontSize = 10.sp,
                    )
                    LineSlider(
                      modifier = Modifier
                        .weight(1f)
                        .height(28.dp),
                      value = progress,
                      onValueChange = { v ->
                        if (index < bandProgress.size) {
                          bandProgress[index] = v
                        }
                        selectedPreset = -1
                        if (enableEq) {
                          EQHelper.setBandLevel(index, (v + minLevel).roundToInt())
                        }
                      },
                      valueRange = 0f..levelRange,
                      properties = defaultLineSliderProperties.copy(
                        trackHeight = 4.dp,
                        thumbWidth = 12.dp,
                        thumbHeight = 12.dp,
                      ),
                      enabled = enableEq,
                    )
                    Text(
                      text = maxLevelText,
                      color = theme.textMuted,
                      fontSize = 10.sp,
                    )
                    Text(
                      text = if (db > 0) "+$db" else "$db",
                      color = theme.textPrimary,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Medium,
                      textAlign = TextAlign.End,
                      modifier = Modifier.width(28.dp),
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun formatFreq(milliHz: Int): String {
  val hz = milliHz / 1000
  return if (hz >= 1000) {
    String.format(Locale.US, "%.1fk", hz / 1000f)
  } else {
    "${hz}Hz"
  }
}
