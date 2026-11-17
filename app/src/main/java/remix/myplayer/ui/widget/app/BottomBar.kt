package remix.myplayer.ui.widget.app

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import remix.myplayer.R
import remix.myplayer.helper.SleepTimer
import remix.myplayer.service.Command
import remix.myplayer.service.MusicService
import remix.myplayer.service.MusicService.Companion.EXTRA_COMMAND
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.theme.icon
import remix.myplayer.ui.widget.common.LineSlider
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.ui.widget.common.TextSecondary
import remix.myplayer.ui.widget.common.defaultLineSliderProperties
import remix.myplayer.ui.widget.library.GlideCover
import remix.myplayer.util.Util
import remix.myplayer.util.ext.clickableWithoutRipple
import remix.myplayer.viewmodel.PlaybackViewModel
import remix.myplayer.viewmodel.PlayingScreenValue
import remix.myplayer.viewmodel.mainViewModel
import remix.myplayer.viewmodel.playbackViewModel
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.roundToLong

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

import kotlinx.coroutines.delay

private const val triggerThreshold = 10

@Composable
fun BottomBar(modifier: Modifier = Modifier, vm: PlaybackViewModel = playbackViewModel) {
  val mainVM = mainViewModel
  val scope = rememberCoroutineScope()
  val playbackState by vm.playbackUiState.collectAsStateWithLifecycle()
  val seekBarUiState by vm.seekBarUiState.collectAsStateWithLifecycle()
  val theme = LocalTheme.current
  val interactionSource = remember { MutableInteractionSource() }

  var hasTriggerAct by remember { mutableStateOf(false) }
  var hasTriggerOp by remember { mutableStateOf(false) }

  val isSongValid = playbackState.song.valid()
  val accent = theme.secondary
  val surface = theme.surface
  val controlTint = theme.icon()
  val divider = theme.divider
  val timerRunning by SleepTimer.runningState.collectAsStateWithLifecycle()
  val timerRemainMin = if (timerRunning) {
    ceil(SleepTimer.getMillisUntilFinish() / 60_000.0).toInt().coerceAtLeast(1)
  } else {
    0
  }

  val expandGestures = if (isSongValid) {
    Modifier
      .clickableWithoutRipple(interactionSource) {
        scope.launch {
          mainVM.playingScreenState.animateTo(PlayingScreenValue.Expanded)
        }
      }
      .pointerInput(Unit) {
        detectVerticalDragGestures(
          onDragStart = { hasTriggerAct = false }
        ) { _, dragAmount ->
          if (dragAmount < -triggerThreshold && !hasTriggerAct) {
            hasTriggerAct = true
            scope.launch {
              mainVM.playingScreenState.animateTo(PlayingScreenValue.Expanded)
            }
          }
        }
      }
      .pointerInput(Unit) {
        detectHorizontalDragGestures(
          onDragStart = { hasTriggerOp = false }
        ) { _, dragAmount ->
          if (dragAmount.absoluteValue > triggerThreshold && !hasTriggerOp) {
            hasTriggerOp = true
            Util.sendLocalBroadcast(
              Intent(MusicService.ACTION_CMD)
                .putExtra(
                  EXTRA_COMMAND,
                  if (dragAmount < 0) Command.SKIP_TO_NEXT else Command.SKIP_TO_PREVIOUS
                )
            )
          }
        }
      }
  } else {
    Modifier
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 6.dp)
      .clip(RoundedCornerShape(16.dp))
      .border(BorderStroke(1.dp, divider.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
      .background(surface)
      .semantics { contentDescription = "BottomBar" }
  ) {
    if (isSongValid) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, start = 10.dp, end = 10.dp)
          .height(20.dp),
        contentAlignment = Alignment.Center
      ) {
        ProgressAware { progress, duration ->
          val value = if (seekBarUiState.interacting && seekBarUiState.uiProgress != null) {
            seekBarUiState.uiProgress!!.toFloat()
          } else {
            progress.toFloat()
          }
          LineSlider(
            value = value,
            onValueChange = { v ->
              vm.setSeekbarUiState(v.roundToLong(), true)
            },
            onValueChangeFinished = {
              vm.setProgress(vm.seekBarUiState.value.uiProgress ?: progress)
              scope.launch {
                delay(400)
                vm.setSeekbarUiState(null, false)
              }
            },
            valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
            modifier = Modifier.fillMaxWidth(),
            properties = defaultLineSliderProperties.copy(
              trackHeight = 3.5.dp,
              trackProgressColor = accent,
              trackBackgroundColor = divider.copy(alpha = 0.4f),
              thumbColor = accent,
              thumbWidth = 12.dp,
              thumbHeight = 12.dp,
              thumbShape = CircleShape,
            ),
          )
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .then(expandGestures),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      GlideCover(
        model = playbackState.song,
        circle = false,
        modifier = Modifier
          .padding(start = 10.dp)
          .size(42.dp)
          .clip(RoundedCornerShape(10.dp))
      )

      Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 10.dp)
      ) {
        TextPrimary(
          text = playbackState.song.showName,
          fontSize = 14.5.sp,
          fontWeight = FontWeight.SemiBold
        )
        val cleanArtist = playbackState.song.cleanArtist
        if (cleanArtist.isNotEmpty() || timerRunning) {
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (cleanArtist.isNotEmpty()) {
              TextSecondary(
                text = cleanArtist,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f, fill = false)
              )
            }
            if (timerRunning) {
              val prefix = if (cleanArtist.isNotEmpty()) " · " else ""
              TextPrimary(
                text = "$prefix${timerRemainMin}m",
                fontSize = 11.5.sp,
                color = accent
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.16f))
            .semantics { contentDescription = "Play or pause" }
            .clickableWithoutRipple(interactionSource) {
              Util.sendLocalBroadcast(
                Intent(MusicService.ACTION_CMD)
                  .putExtra(EXTRA_COMMAND, Command.PLAY_PAUSE)
              )
            }
        ) {
          Icon(
            painter = painterResource(
              if (playbackState.isPlaying) {
                R.drawable.ic_pause_black_24dp
              } else {
                R.drawable.ic_play_arrow_black_24dp
              }
            ),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp)
          )
        }
        Icon(
          modifier = Modifier
            .size(40.dp)
            .padding(8.dp)
            .clickableWithoutRipple(interactionSource) {
              Util.sendLocalBroadcast(
                Intent(MusicService.ACTION_CMD)
                  .putExtra(EXTRA_COMMAND, Command.SKIP_TO_NEXT)
              )
            },
          painter = painterResource(R.drawable.play_btn_next),
          contentDescription = "Next track",
          tint = controlTint
        )
      }
    }
  }
}
