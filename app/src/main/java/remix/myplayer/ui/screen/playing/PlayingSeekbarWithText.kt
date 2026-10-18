package remix.myplayer.ui.screen.playing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.app.ProgressAware
import remix.myplayer.ui.widget.common.LineSlider
import remix.myplayer.ui.widget.common.defaultLineSliderProperties
import remix.myplayer.util.Util
import remix.myplayer.viewmodel.playbackViewModel
import kotlin.math.roundToLong

@Composable
internal fun PlayingSeekbarWithText(
  swatch: Palette.Swatch
) {
  val playbackVM = playbackViewModel
  val scope = rememberCoroutineScope()

  ProgressAware { progress, duration ->
    val seekBarUiState by playbackVM.seekBarUiState.collectAsStateWithLifecycle()

    val timeLabel = remember(seekBarUiState, progress, duration) {
      if (duration <= 0) {
        "00:00 / 00:00"
      } else {
        val elapsed = seekBarUiState.uiProgress ?: progress
        "${Util.getTime(elapsed)} / ${Util.getTime(duration)}"
      }
    }

    val theme = LocalTheme.current
    val trackBg = theme.control
    val progressColor = Color(swatch.rgb)
    val thumbColor = if (theme.isLight) theme.textPrimary else Color.White
    val baseProperties = defaultLineSliderProperties
    val sliderProperties = remember(swatch.rgb, trackBg, thumbColor) {
      baseProperties.copy(
        trackBackgroundColor = trackBg,
        trackProgressColor = progressColor,
        trackHeight = 8.dp,
        thumbColor = thumbColor,
        thumbWidth = 18.dp,
        thumbHeight = 18.dp,
        thumbShape = CircleShape
      )
    }

    val tabularStyle = remember {
      TextStyle(
        fontSize = 12.sp,
        fontFeatureSettings = "tnum"
      )
    }

    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      LineSlider(
        value = if (seekBarUiState.interacting && seekBarUiState.uiProgress != null) {
          seekBarUiState.uiProgress!!.toFloat()
        } else {
          progress.toFloat()
        },
        onValueChange = { v ->
          playbackVM.setSeekbarUiState(v.roundToLong(), true)
        },
        onValueChangeFinished = {
          playbackVM.setProgress(playbackVM.seekBarUiState.value.uiProgress ?: progress)
          scope.launch {
            delay(400)
            playbackVM.setSeekbarUiState(null, false)
          }
        },
        valueRange = 0f..duration.toFloat().coerceAtLeast(0f),
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp)
          .padding(horizontal = 4.dp),
        properties = sliderProperties
      )

      Text(
        text = timeLabel,
        style = tabularStyle,
        maxLines = 1,
        color = theme.textMuted,
        modifier = Modifier.padding(top = 2.dp)
      )
    }
  }
}
