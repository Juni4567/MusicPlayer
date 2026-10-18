package remix.myplayer.ui.screen

import android.media.audiofx.AudioEffect
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.launch
import remix.myplayer.R
import remix.myplayer.helper.EQHelper
import remix.myplayer.service.MusicServiceRemote
import remix.myplayer.ui.nav.ExtraRestorePlayingScreen
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.MessageNotifier
import remix.myplayer.ui.theme.BrandColors
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.CommonAppBar
import remix.myplayer.ui.widget.common.LineSlider
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.ui.widget.common.TextSecondary
import remix.myplayer.ui.widget.common.defaultLineSliderProperties
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.PlayingScreenValue
import remix.myplayer.viewmodel.mainViewModel
import java.text.DecimalFormat
import kotlin.math.roundToInt

private data class SoftEqPreset(
  val nameRes: Int,
  /** Relative gains in [-1, 1] from low → high frequency. */
  val gains: FloatArray,
)

/** Software fallbacks used when the device exposes no built-in EQ presets. */
private val SoftEqPresets = listOf(
  SoftEqPreset(R.string.eq_preset_flat, floatArrayOf(0f, 0f, 0f, 0f, 0f)),
  SoftEqPreset(R.string.eq_preset_rock, floatArrayOf(0.45f, 0.25f, -0.1f, 0.2f, 0.4f)),
  SoftEqPreset(R.string.eq_preset_pop, floatArrayOf(-0.15f, 0.25f, 0.35f, 0.2f, -0.1f)),
  SoftEqPreset(R.string.eq_preset_classical, floatArrayOf(0.4f, 0.25f, -0.2f, 0.25f, 0.35f)),
  SoftEqPreset(R.string.eq_preset_vocal, floatArrayOf(-0.2f, 0.1f, 0.45f, 0.35f, 0.1f)),
  SoftEqPreset(R.string.eq_preset_jazz, floatArrayOf(0.3f, 0.1f, -0.15f, 0.15f, 0.35f)),
  SoftEqPreset(R.string.eq_preset_dance, floatArrayOf(0.5f, 0.35f, 0.05f, -0.1f, 0.25f)),
  SoftEqPreset(R.string.eq_preset_bass, floatArrayOf(0.7f, 0.45f, 0.1f, -0.15f, -0.2f)),
  SoftEqPreset(R.string.eq_preset_treble, floatArrayOf(-0.25f, -0.1f, 0.15f, 0.45f, 0.65f)),
)

private sealed class EqPresetOption {
  data class System(val index: Int, val name: String) : EqPresetOption()
  data class Soft(val index: Int, val preset: SoftEqPreset) : EqPresetOption()
  data object Custom : EqPresetOption()
}

@Composable
fun EQScreen(backStackEntry: NavBackStackEntry) {
  val context = LocalContext.current
  val nav = LocalNavController.current
  val scope = rememberCoroutineScope()
  val mainVM = mainViewModel
  val theme = LocalTheme.current

  fun exitEq() {
    scope.launch {
      if (backStackEntry.savedStateHandle.get<Boolean>(ExtraRestorePlayingScreen) == true) {
        mainVM.playingScreenState.snapTo(PlayingScreenValue.Expanded)
      }
      nav.popBackStack()
    }
  }

  BackHandler { exitEq() }

  val sessionId = MusicServiceRemote.getAudioSessionId()
  if (sessionId == AudioEffect.ERROR_BAD_VALUE || sessionId == null) {
    MessageNotifier.show(R.string.no_audio_ID)
    nav.popBackStack()
    return
  }

  if (!EQHelper.init(context, sessionId, true)) {
    MessageNotifier.show(R.string.eq_initial_failed)
    nav.popBackStack()
    return
  }

  var enableEq by remember { mutableStateOf(EQHelper.enable) }
  val bandNumber = EQHelper.bandNumber.toInt()
  val minLevel = EQHelper.minLevel
  val maxLevel = EQHelper.maxLevel
  val levelRange = (maxLevel - minLevel).toFloat()

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

  val systemNames = remember { EQHelper.systemPresetNames() }
  val presetOptions = remember(systemNames) {
    buildList {
      if (systemNames.isNotEmpty()) {
        systemNames.forEachIndexed { index, name ->
          add(EqPresetOption.System(index, name))
        }
      } else {
        SoftEqPresets.forEachIndexed { index, preset ->
          add(EqPresetOption.Soft(index, preset))
        }
      }
      add(EqPresetOption.Custom)
    }
  }

  // Start on Custom; selecting a chip applies that preset as the new base.
  var selectedPresetIndex by remember { mutableIntStateOf(presetOptions.lastIndex) }

  fun refreshBandUi() {
    repeat(bandNumber) { i ->
      bandProgress[i] = (EQHelper.getBandLevel(i) - minLevel).toFloat()
    }
  }

  fun applyPreset(option: EqPresetOption, index: Int) {
    when (option) {
      is EqPresetOption.System -> EQHelper.useSystemPreset(option.index)
      is EqPresetOption.Soft -> EQHelper.applyRelativeGains(option.preset.gains)
      EqPresetOption.Custom -> Unit
    }
    if (option !is EqPresetOption.Custom) {
      selectedPresetIndex = index
      refreshBandUi()
    }
  }

  fun markCustomFromUserEdit() {
    selectedPresetIndex = presetOptions.lastIndex
  }

  Scaffold(
    contentWindowInsets = WindowInsets.systemBars,
    topBar = {
      CommonAppBar(
        title = stringResource(R.string.eq),
        onBack = { exitEq() },
        actions = emptyList()
      )
    },
    containerColor = theme.mainBackground,
  ) { contentPadding ->
    Column(
      modifier = Modifier
        .padding(contentPadding)
        .fillMaxSize()
        .navigationBarsPadding(),
    ) {
      // Top: enable + presets (easy to reach after opening EQ)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextSecondary(stringResource(R.string.use_eq), fontSize = 14.sp)
          Switch(
            modifier = Modifier.scale(0.85f),
            checked = enableEq,
            colors = SwitchDefaults.colors().copy(
              checkedTrackColor = theme.secondary,
              uncheckedTrackColor = Color.Transparent
            ),
            onCheckedChange = { checked ->
              enableEq = checked
              EQHelper.updateEnable(checked)
              if (!EQHelper.isBassBoostEnabled) {
                bassStrength = EQHelper.bassBoostStrength.toFloat()
              }
            }
          )
        }

        TextSecondary(
          stringResource(R.string.eq_presets),
          fontSize = 13.sp,
          modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          presetOptions.forEachIndexed { index, option ->
            val label = when (option) {
              is EqPresetOption.System -> option.name
              is EqPresetOption.Soft -> stringResource(option.preset.nameRes)
              EqPresetOption.Custom -> stringResource(R.string.custom)
            }
            EqPresetChip(
              label = label,
              selected = selectedPresetIndex == index,
              enabled = enableEq,
              onClick = {
                if (option is EqPresetOption.Custom) {
                  selectedPresetIndex = index
                } else {
                  applyPreset(option, index)
                }
              }
            )
          }
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
        ) {
          TextSecondary(stringResource(R.string.bass_boost), fontSize = 14.sp)
          LineSlider(
            modifier = Modifier
              .fillMaxWidth()
              .height(36.dp),
            value = bassStrength,
            onValueChange = { v ->
              bassStrength = v
              if (EQHelper.isBassBoostEnabled) {
                EQHelper.bassBoostStrength = v.roundToInt()
              }
            },
            valueRange = 0f..1000f,
            properties = defaultLineSliderProperties.copy(
              trackHeight = 6.dp,
              thumbWidth = 16.dp,
              thumbHeight = 16.dp,
            ),
            enabled = EQHelper.isBassBoostEnabled
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Bottom (thumb zone): band bars
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(BrandColors.Surface)
          .padding(horizontal = 16.dp, vertical = 12.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        TextPrimary(
          stringResource(R.string.eq),
          fontSize = 14.sp,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
        )

        repeat(bandNumber) { index ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            TextSecondary(
              formatBandFreq(EQHelper.getCenterFreq(index)),
              fontSize = 12.sp
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              TextPrimary(minLevelText, fontSize = 11.sp)
              LineSlider(
                modifier = Modifier
                  .weight(1f)
                  .height(40.dp),
                value = bandProgress[index],
                onValueChange = { v ->
                  bandProgress[index] = v
                  markCustomFromUserEdit()
                  if (enableEq) {
                    EQHelper.setBandLevel(index, (v + minLevel).roundToInt())
                  }
                },
                valueRange = 0f..levelRange,
                properties = defaultLineSliderProperties.copy(
                  trackHeight = 8.dp,
                  thumbWidth = 18.dp,
                  thumbHeight = 18.dp,
                ),
                enabled = enableEq
              )
              TextPrimary(maxLevelText, fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EqPresetChip(
  label: String,
  selected: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  val theme = LocalTheme.current
  val bg = when {
    selected -> theme.secondary
    else -> BrandColors.Surface2
  }
  val border = if (selected) Color.Transparent else BrandColors.Divider
  val textColor = when {
    selected -> Color.White
    enabled -> theme.textPrimary
    else -> theme.textSecondary
  }

  Text(
    text = label,
    color = textColor,
    fontSize = 13.sp,
    textAlign = TextAlign.Center,
    maxLines = 1,
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(bg)
      .border(1.dp, border, RoundedCornerShape(20.dp))
      .clickWithRipple(circle = false, enabled = enabled, onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp)
  )
}

private fun formatBandFreq(milliHz: Int): String {
  val hz = milliHz / 1000
  return if (hz >= 1000) {
    String.format("%.1f kHz", hz / 1000f)
  } else {
    "$hz Hz"
  }
}
