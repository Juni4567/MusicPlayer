package remix.myplayer.ui.dialog

import android.media.audiofx.AudioEffect
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import remix.myplayer.helper.EQHelper
import remix.myplayer.service.MusicServiceRemote
import remix.myplayer.ui.nav.MessageNotifier
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.LineSlider
import remix.myplayer.ui.widget.common.defaultLineSliderProperties
import remix.myplayer.util.ext.clickWithRipple
import java.text.DecimalFormat
import kotlin.math.min
import kotlin.math.roundToInt

private data class SheetEqPreset(val nameRes: Int, val gains: FloatArray)

private val SheetEqPresets = listOf(
  SheetEqPreset(R.string.eq_preset_flat, floatArrayOf(0f, 0f, 0f, 0f, 0f)),
  SheetEqPreset(R.string.eq_preset_rock, floatArrayOf(0.45f, 0.25f, -0.1f, 0.2f, 0.4f)),
  SheetEqPreset(R.string.eq_preset_pop, floatArrayOf(-0.15f, 0.25f, 0.35f, 0.2f, -0.1f)),
  SheetEqPreset(R.string.eq_preset_classical, floatArrayOf(0.4f, 0.25f, -0.2f, 0.25f, 0.35f)),
  SheetEqPreset(R.string.eq_preset_vocal, floatArrayOf(-0.2f, 0.1f, 0.45f, 0.35f, 0.1f)),
  SheetEqPreset(R.string.eq_preset_jazz, floatArrayOf(0.3f, 0.1f, -0.15f, 0.15f, 0.35f)),
  SheetEqPreset(R.string.eq_preset_dance, floatArrayOf(0.5f, 0.35f, 0.05f, -0.1f, 0.25f)),
  SheetEqPreset(R.string.eq_preset_bass, floatArrayOf(0.7f, 0.45f, 0.1f, -0.15f, -0.2f)),
  SheetEqPreset(R.string.eq_preset_treble, floatArrayOf(-0.25f, -0.1f, 0.15f, 0.45f, 0.65f)),
)

@Composable
fun EqualizerSheet(state: DialogState) {
  if (!state.isOpen) return

  val context = LocalContext.current
  val theme = LocalTheme.current
  val sessionId = MusicServiceRemote.getAudioSessionId()

  if (sessionId == null || sessionId == AudioEffect.ERROR_BAD_VALUE) {
    MessageNotifier.show(R.string.no_audio_ID)
    state.dismiss()
    return
  }
  if (!EQHelper.init(context, sessionId, true)) {
    MessageNotifier.show(R.string.eq_initial_failed)
    state.dismiss()
    return
  }

  var enableEq by remember { mutableStateOf(EQHelper.enable) }
  val bandNumber = EQHelper.bandNumber.toInt().coerceAtLeast(1)
  val displayBands = min(5, bandNumber)
  val minLevel = EQHelper.minLevel
  val maxLevel = EQHelper.maxLevel
  val levelRange = (maxLevel - minLevel).toFloat().coerceAtLeast(1f)
  val decimalFormat = remember { DecimalFormat("+#;-#") }
  val minLevelText = decimalFormat.format(minLevel / 100)
  val maxLevelText = decimalFormat.format(maxLevel / 100)

  val bandProgress = remember(bandNumber, minLevel) {
    mutableStateListOf<Float>().apply {
      repeat(bandNumber) { i ->
        add((EQHelper.getBandLevel(i) - minLevel).toFloat())
      }
    }
  }
  var bassStrength by remember { mutableFloatStateOf(EQHelper.bassBoostStrength.toFloat()) }
  var virtualizerStrength by remember {
    mutableFloatStateOf(EQHelper.virtualizerStrength.toFloat())
  }
  var selectedPreset by remember { mutableIntStateOf(-1) }

  fun refreshBands() {
    repeat(bandNumber) { i ->
      bandProgress[i] = (EQHelper.getBandLevel(i) - minLevel).toFloat()
    }
  }

  fun resetEq() {
    EQHelper.reset()
    selectedPreset = 0
    bassStrength = 0f
    virtualizerStrength = 0f
    refreshBands()
  }

  MiBottomSheet(
    visible = true,
    onDismiss = { state.dismiss() },
    title = stringResource(R.string.eq),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 460.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
            text = stringResource(R.string.reset),
            color = theme.secondary,
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
              checkedTrackColor = theme.secondary,
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

      Column {
        SheetSectionLabel(stringResource(R.string.eq_presets))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          SheetEqPresets.forEachIndexed { index, preset ->
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

      SheetSliderRow(
        label = stringResource(R.string.bass_boost),
        valueLabel = "${(bassStrength / 10f).roundToInt()}%",
        value = bassStrength,
        valueRange = 0f..1000f,
        enabled = EQHelper.isBassBoostEnabled,
        onValueChange = { v ->
          bassStrength = v
          if (EQHelper.isBassBoostEnabled) {
            EQHelper.bassBoostStrength = v.roundToInt()
          }
        },
      )

      if (EQHelper.isVirtualizerSupported) {
        SheetSliderRow(
          label = stringResource(R.string.virtualizer),
          valueLabel = "${(virtualizerStrength / 10f).roundToInt()}%",
          value = virtualizerStrength,
          valueRange = 0f..1000f,
          enabled = EQHelper.isVirtualizerEnabled,
          onValueChange = { v ->
            virtualizerStrength = v
            EQHelper.virtualizerStrength = v.roundToInt()
          },
        )
      } else {
        Text(
          text = stringResource(R.string.virtualizer_unavailable_hint),
          color = theme.textMuted,
          fontSize = 12.sp,
        )
      }

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
              text = formatBandFreq(EQHelper.getCenterFreq(index)),
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

private fun formatBandFreq(milliHz: Int): String {
  val hz = milliHz / 1000
  return if (hz >= 1000) {
    String.format("%.1fk", hz / 1000f)
  } else {
    "${hz}Hz"
  }
}
