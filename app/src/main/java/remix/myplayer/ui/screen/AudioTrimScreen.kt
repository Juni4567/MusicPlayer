@file:OptIn(ExperimentalMaterial3Api::class)

package remix.myplayer.ui.screen

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.launch
import remix.myplayer.R
import remix.myplayer.ui.activity.base.BaseActivity
import remix.myplayer.ui.nav.ExtraRestorePlayingScreen
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.CommonAppBar
import remix.myplayer.util.Util
import remix.myplayer.util.ext.clickableWithoutRipple
import remix.myplayer.viewmodel.PlayingScreenValue
import remix.myplayer.viewmodel.audioTrimViewModel
import remix.myplayer.viewmodel.mainViewModel
import kotlin.math.abs
import kotlin.math.max

@Composable
fun AudioTrimScreen(backStackEntry: NavBackStackEntry) {
  val trimVM = audioTrimViewModel
  val state by trimVM.state.collectAsStateWithLifecycle()
  val navController = LocalNavController.current
  val song = state.song

  if (song == null || !song.valid() || !song.isLocal()) {
    LaunchedEffect(Unit) {
      navController.popBackStack()
    }
    return
  }

  val activity = LocalActivity.current as? BaseActivity ?: return
  val scope = rememberCoroutineScope()
  val mainVM = mainViewModel
  val theme = LocalTheme.current

  fun exitTrim() {
    scope.launch {
      trimVM.stopPreview()
      if (backStackEntry.savedStateHandle.get<Boolean>(ExtraRestorePlayingScreen) == true) {
        mainVM.playingScreenState.snapTo(PlayingScreenValue.Expanded)
      }
      navController.popBackStack()
    }
  }

  LaunchedEffect(state.saveCompleted) {
    if (state.saveCompleted) {
      exitTrim()
    }
  }

  BackHandler { exitTrim() }

  Scaffold(
    contentWindowInsets = WindowInsets.systemBars,
    topBar = {
      CommonAppBar(
        title = stringResource(R.string.audio_trim_title),
        onBack = { exitTrim() },
        actions = emptyList()
      )
    },
    floatingActionButton = {
      Box(
        modifier = Modifier
          .size(48.dp)
          .background(
            color = if (state.isSaving) theme.textSecondary else theme.secondary,
            shape = CircleShape
          )
          .clickableWithoutRipple(enabled = !state.isSaving) {
            trimVM.save(activity)
          },
        contentAlignment = Alignment.Center
      ) {
        if (state.isSaving) {
          CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp,
            color = Color.White
          )
        } else {
          Icon(
            painterResource(R.drawable.ic_save_white_24dp),
            contentDescription = stringResource(R.string.audio_trim_save),
            tint = Color.White
          )
        }
      }
    },
    containerColor = theme.mainBackground
  ) { contentPadding ->
    Column(
      modifier = Modifier
        .padding(contentPadding)
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = song.showName,
        color = theme.textPrimary,
        fontSize = 18.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = song.artist,
        color = theme.textSecondary,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = "${state.selectionLabel}  /  ${Util.getTime(state.durationMs)}",
        color = theme.textPrimary,
        fontSize = 15.sp
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
          .background(theme.dialogBackground, RoundedCornerShape(8.dp))
          .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        if (state.isLoadingWaveform || state.waveform.isEmpty()) {
          CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = theme.secondary,
            strokeWidth = 2.dp
          )
        } else {
          WaveformTrimCanvas(
            peaks = state.waveform,
            durationMs = state.durationMs,
            startMs = state.startMs,
            endMs = state.endMs,
            previewMs = if (state.isPreviewing) state.previewPositionMs else null,
            selectionColor = theme.secondary.copy(alpha = 0.35f),
            barColor = theme.textSecondary.copy(alpha = 0.55f),
            handleColor = theme.secondary,
            previewColor = theme.primary,
            onStartChanged = { trimVM.setStartMs(it) },
            onEndChanged = { trimVM.setEndMs(it) },
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(onClick = { trimVM.togglePreview() }, enabled = !state.isSaving) {
          Text(
            text = stringResource(
              if (state.isPreviewing) R.string.audio_trim_stop_preview
              else R.string.audio_trim_preview
            ),
            color = theme.secondary
          )
        }
        Text(
          text = "${Util.getTime(state.startMs)} – ${Util.getTime(state.endMs)}",
          color = theme.textSecondary,
          fontSize = 13.sp
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .clickableWithoutRipple(enabled = !state.isSaving) {
            trimVM.setReplaceOriginal(!state.replaceOriginal)
          }
      ) {
        Checkbox(
          checked = state.replaceOriginal,
          onCheckedChange = { trimVM.setReplaceOriginal(it) },
          enabled = !state.isSaving
        )
        Spacer(Modifier.width(4.dp))
        Text(
          text = stringResource(R.string.audio_trim_replace_original),
          color = theme.textPrimary,
          fontSize = 15.sp
        )
      }

      Text(
        text = stringResource(
          if (state.replaceOriginal) R.string.audio_trim_replace_hint
          else R.string.audio_trim_save_as_hint
        ),
        color = theme.textSecondary,
        fontSize = 13.sp
      )
    }
  }
}

@Composable
private fun WaveformTrimCanvas(
  peaks: FloatArray,
  durationMs: Long,
  startMs: Long,
  endMs: Long,
  previewMs: Long?,
  selectionColor: Color,
  barColor: Color,
  handleColor: Color,
  previewColor: Color,
  onStartChanged: (Long) -> Unit,
  onEndChanged: (Long) -> Unit,
) {
  val density = LocalDensity.current
  val handleHitPx = with(density) { 28.dp.toPx() }
  val dragMode = remember { arrayOf(0) } // 0 none, 1 start, 2 end

  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .pointerInput(durationMs, startMs, endMs) {
        detectHorizontalDragGestures(
          onDragStart = { offset ->
            val startX = (startMs.toFloat() / durationMs) * size.width
            val endX = (endMs.toFloat() / durationMs) * size.width
            dragMode[0] = when {
              abs(offset.x - startX) <= handleHitPx -> 1
              abs(offset.x - endX) <= handleHitPx -> 2
              offset.x < (startX + endX) / 2f -> 1
              else -> 2
            }
          },
          onDragEnd = { dragMode[0] = 0 },
          onDragCancel = { dragMode[0] = 0 },
          onHorizontalDrag = { change, _ ->
            change.consume()
            val ms = ((change.position.x / size.width) * durationMs)
              .toLong()
              .coerceIn(0L, durationMs)
            when (dragMode[0]) {
              1 -> onStartChanged(ms)
              2 -> onEndChanged(ms)
            }
          }
        )
      }
  ) {
    val w = size.width
    val h = size.height
    val barCount = peaks.size
    if (barCount == 0 || durationMs <= 0L) return@Canvas

    val gap = 1.5f
    val barWidth = max(1f, (w - gap * (barCount - 1)) / barCount)
    val startX = (startMs.toFloat() / durationMs) * w
    val endX = (endMs.toFloat() / durationMs) * w

    // Dim outside selection.
    drawRect(color = Color.Black.copy(alpha = 0.18f), size = Size(startX, h))
    drawRect(
      color = Color.Black.copy(alpha = 0.18f),
      topLeft = Offset(endX, 0f),
      size = Size(w - endX, h)
    )
    // Selection highlight.
    drawRect(
      color = selectionColor,
      topLeft = Offset(startX, 0f),
      size = Size(max(0f, endX - startX), h)
    )

    for (i in 0 until barCount) {
      val amp = peaks[i].coerceIn(0.05f, 1f)
      val barH = amp * h * 0.9f
      val x = i * (barWidth + gap)
      val y = (h - barH) / 2f
      val inSelection = x + barWidth >= startX && x <= endX
      drawRoundRect(
        color = if (inSelection) handleColor.copy(alpha = 0.85f) else barColor,
        topLeft = Offset(x, y),
        size = Size(barWidth, barH),
        cornerRadius = CornerRadius(1.5f, 1.5f)
      )
    }

    // Handles.
    val handleW = 4.dp.toPx()
    drawRoundRect(
      color = handleColor,
      topLeft = Offset(startX - handleW / 2f, 0f),
      size = Size(handleW, h),
      cornerRadius = CornerRadius(2f, 2f)
    )
    drawRoundRect(
      color = handleColor,
      topLeft = Offset(endX - handleW / 2f, 0f),
      size = Size(handleW, h),
      cornerRadius = CornerRadius(2f, 2f)
    )
    // Handle knobs.
    val knobR = 7.dp.toPx()
    drawCircle(handleColor, knobR, Offset(startX, h / 2f))
    drawCircle(handleColor, knobR, Offset(endX, h / 2f))

    previewMs?.let { ms ->
      val px = (ms.toFloat() / durationMs) * w
      drawLine(
        color = previewColor,
        start = Offset(px, 0f),
        end = Offset(px, h),
        strokeWidth = 2.dp.toPx()
      )
    }
  }
}
