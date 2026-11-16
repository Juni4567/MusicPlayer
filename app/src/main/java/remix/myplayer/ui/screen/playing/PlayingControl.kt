package remix.myplayer.ui.screen.playing

import android.content.Intent
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import kotlinx.coroutines.launch
import remix.myplayer.R
import remix.myplayer.data.prefs.SettingPrefs.Companion.MODE_LOOP
import remix.myplayer.data.prefs.SettingPrefs.Companion.MODE_REPEAT
import remix.myplayer.data.prefs.SettingPrefs.Companion.MODE_SHUFFLE
import remix.myplayer.service.Command
import remix.myplayer.service.MusicService
import remix.myplayer.service.MusicService.Companion.EXTRA_POSITION
import remix.myplayer.service.playback.PlaybackUiState
import remix.myplayer.ui.dialog.BottomDialog
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.ui.widget.common.TextSecondary
import remix.myplayer.ui.widget.library.GlideCover
import remix.myplayer.ui.widget.playpause.PlayPauseView
import remix.myplayer.util.MusicUtil.makeCmdIntent
import remix.myplayer.util.Util
import remix.myplayer.util.Util.sendLocalBroadcast
import remix.myplayer.util.ext.CenterInBox
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.util.ext.isPortraitOrientation
import remix.myplayer.viewmodel.PlayingScreenValue
import remix.myplayer.viewmodel.mainViewModel
import remix.myplayer.viewmodel.playbackViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val itemRes = mapOf(
  MODE_LOOP to Pair(R.drawable.play_btn_loop, R.string.model_normal),
  MODE_SHUFFLE to Pair(R.drawable.play_btn_shuffle, R.string.model_random),
  MODE_REPEAT to Pair(R.drawable.play_btn_loop_one, R.string.model_repeat)
)

@Composable
internal fun PlayingControl(
  modifier: Modifier = Modifier,
  playbackUiState: PlaybackUiState,
  @Suppress("UNUSED_PARAMETER") swatch: Palette.Swatch,
  iconSize: Dp? = null,
  playPauseSize: Dp = 56.dp,
  buttonSize: Dp? = null
) {
  Row(
    modifier = modifier
      .fillMaxSize(),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    val theme = LocalTheme.current
    val accentColor = theme.secondary
    val playMode = playbackUiState.playMode
    val viewModel = playbackViewModel
    val iconModifier = if (iconSize != null) Modifier.size(iconSize) else Modifier
    ControlButton(
      onClick = {
        Util.sendCMDLocalBroadcast(Command.CHANGE_MODEL)
      },
      buttonSize = buttonSize
    ) {
      Image(
        modifier = iconModifier,
        painter = painterResource(itemRes[playMode]!!.first),
        contentDescription = "PlayingMode",
        colorFilter = ColorFilter.tint(accentColor.copy(alpha = 0.85f))
      )
    }

    ControlButton(
      onClick = {
        sendLocalBroadcast(
          Intent(MusicService.ACTION_CMD).putExtra(
            MusicService.EXTRA_COMMAND,
            Command.SKIP_TO_PREVIOUS
          )
        )
      },
      onLongPressStart = {
        viewModel.startContinuousSeek(forward = false)
      },
      onLongPressEnd = {
        viewModel.stopContinuousSeek()
      },
      buttonSize = buttonSize
    ) {
      Image(
        modifier = iconModifier,
        painter = painterResource(R.drawable.play_btn_pre),
        contentDescription = "PlayingPrev",
        colorFilter = ColorFilter.tint(accentColor)
      )
    }

    ControlButton(
      onClick = {
        sendLocalBroadcast(
          Intent(MusicService.ACTION_CMD).putExtra(
            MusicService.EXTRA_COMMAND,
            Command.PLAY_PAUSE
          )
        )
      },
      buttonSize = buttonSize
    ) {
      val size = with(LocalDensity.current) { playPauseSize.roundToPx() }
      val playBgColor = theme.secondary.toArgb()
      AndroidView(
        factory = {
          PlayPauseView(it).apply {
            setBackgroundColor(Color.Transparent.toArgb())
            layoutParams = ViewGroup.LayoutParams(size, size)
          }
        },
        update = {
          it.setBackgroundColor(playBgColor)
          if (it.layoutParams.width != size || it.layoutParams.height != size) {
            it.layoutParams = ViewGroup.LayoutParams(size, size)
          }
          it.updateState(playbackUiState.isPlaying, true)
        }
      )
    }

    ControlButton(
      onClick = {
        sendLocalBroadcast(
          Intent(MusicService.ACTION_CMD).putExtra(
            MusicService.EXTRA_COMMAND,
            Command.SKIP_TO_NEXT
          )
        )
      },
      onLongPressStart = {
        viewModel.startContinuousSeek(forward = true)
      },
      onLongPressEnd = {
        viewModel.stopContinuousSeek()
      },
      buttonSize = buttonSize
    ) {
      Image(
        modifier = iconModifier,
        painter = painterResource(R.drawable.play_btn_next),
        contentDescription = "PlayingNext",
        colorFilter = ColorFilter.tint(accentColor)
      )
    }

    var showPlayQueue by remember { mutableStateOf(false) }
    PlayQueueDialog(
      visible = showPlayQueue,
      onDismissRequest = { showPlayQueue = false },
      musicState = playbackUiState
    )

    ControlButton(
      onClick = {
        showPlayQueue = true
      },
      buttonSize = buttonSize
    ) {
      Image(
        modifier = iconModifier,
        painter = painterResource(R.drawable.play_btn_normal_list),
        contentDescription = "PlayingPlayQueue",
        colorFilter = ColorFilter.tint(accentColor.copy(alpha = 0.85f))
      )
    }
  }
}

@Composable
private fun PlayQueueDialog(
  visible: Boolean,
  onDismissRequest: () -> Unit,
  musicState: PlaybackUiState
) {
  val playbackVM = playbackViewModel
  val playbackState by playbackVM.playbackUiState.collectAsStateWithLifecycle()
  val songs by playbackVM.playQueueSongs.collectAsStateWithLifecycle()
  val theme = LocalTheme.current
  val surface = theme.surface
  val surface2 = theme.surface2
  val textSecondaryColor = theme.textSecondary
  val textMutedColor = theme.textMuted
  val accent = theme.secondary
  val mainVM = mainViewModel
  val scope = rememberCoroutineScope()

  BottomDialog(
    visible = visible,
    onDismissRequest = onDismissRequest,
    maxHeightFraction = 0.82f
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(surface)
        .padding(bottom = 12.dp)
    ) {
      // Top drag handle indicator
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .width(36.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(theme.divider.copy(alpha = 0.5f))
        )
      }

      // Sleek Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CenterInBox(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(surface2)
            .clickWithRipple { onDismissRequest() }
        ) {
          Image(
            painter = painterResource(R.drawable.icon_player_back),
            contentDescription = "Collapse up next",
            colorFilter = ColorFilter.tint(textSecondaryColor),
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          TextPrimary(
            text = stringResource(R.string.up_next),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
          )
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(accent.copy(alpha = 0.16f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = stringResource(R.string.songs_count_tracks, songs.size),
              color = accent,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        if (songs.size > 1) {
          Text(
            text = stringResource(R.string.clear_queue),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textSecondaryColor,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(surface2)
              .clickWithRipple { playbackVM.clearUpcomingFromQueue() }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      val currentSong = musicState.song
      if (currentSong.valid()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.40f), RoundedCornerShape(14.dp))
            .padding(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accent)
            )
            Text(
              text = stringResource(R.string.now_playing_badge),
              color = accent,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
          ) {
            GlideCover(
              model = currentSong,
              circle = false,
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
            )
            Column(
              modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
            ) {
              TextPrimary(
                text = currentSong.showName,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.height(2.dp))
              TextSecondary(
                text = currentSong.cleanArtist.ifEmpty { "—" },
                fontSize = 12.sp
              )
            }
          }
        }
      }

      val lazyState = rememberLazyListState()
      val reorderableState = rememberReorderableLazyListState(lazyState) { from, to ->
        playbackVM.moveInQueue(from.index, to.index)
      }

      if (songs.isEmpty()) {
        CenterInBox(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 24.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
              painter = painterResource(R.drawable.play_btn_normal_list),
              contentDescription = null,
              colorFilter = ColorFilter.tint(textMutedColor),
              modifier = Modifier.size(48.dp)
            )
            TextPrimary(
              stringResource(R.string.nothing_queued_next),
              fontSize = 16.sp,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 12.dp)
            )
            TextSecondary(
              stringResource(R.string.queue_empty_hint),
              fontSize = 13.sp,
              textAlign = TextAlign.Center,
              maxLine = 2,
              modifier = Modifier.padding(top = 6.dp)
            )
            TextPrimary(
              stringResource(R.string.go_to_library),
              fontSize = 14.sp,
              color = theme.secondary,
              modifier = Modifier
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(theme.secondary.copy(alpha = 0.16f))
                .clickWithRipple(circle = false) {
                  onDismissRequest()
                  scope.launch {
                    mainVM.playingScreenState.animateTo(PlayingScreenValue.Hidden)
                  }
                }
                .padding(horizontal = 18.dp, vertical = 10.dp)
            )
          }
        }
      } else {
        LazyColumn(
          state = lazyState,
          contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
          modifier = Modifier.heightIn(max = 420.dp)
        ) {
          itemsIndexed(songs, key = { _, song -> song.id }) { pos, song ->
            val isCurrent = song.id == musicState.song.id
            ReorderableItem(reorderableState, key = song.id) {
              val cardBg = if (isCurrent) accent.copy(alpha = 0.14f) else surface2.copy(alpha = 0.65f)
              val cardBorder = if (isCurrent) accent else theme.divider.copy(alpha = 0.35f)

              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 3.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(cardBg)
                  .border(0.8.dp, cardBorder, RoundedCornerShape(12.dp))
                  .clickWithRipple(false) {
                    sendLocalBroadcast(
                      makeCmdIntent(Command.PLAY_AT)
                        .putExtra(EXTRA_POSITION, pos)
                    )
                    onDismissRequest()
                  }
                  .padding(horizontal = 10.dp, vertical = 8.dp)
              ) {
                if (song.valid()) {
                  GlideCover(
                    model = song,
                    circle = false,
                    modifier = Modifier
                      .size(42.dp)
                      .clip(RoundedCornerShape(8.dp))
                  )
                }

                Column(
                  modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .weight(1f)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCurrent) {
                      Box(
                        modifier = Modifier
                          .padding(end = 6.dp)
                          .size(5.dp)
                          .clip(CircleShape)
                          .background(accent)
                      )
                    }
                    TextPrimary(
                      if (!song.valid()) stringResource(R.string.song_lose_effect) else song.showName,
                      color = if (isCurrent) accent else theme.textPrimary,
                      fontSize = 14.sp,
                      fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium
                    )
                  }
                  if (song.valid()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    TextSecondary(
                      text = song.cleanArtist.ifEmpty { "—" },
                      fontSize = 11.5.sp
                    )
                  }
                }

                if (song.valid() && !isCurrent) {
                  CenterInBox(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .clickWithRipple { playbackVM.removeFromQueue(song.id) }
                  ) {
                    Image(
                      painter = painterResource(R.drawable.ic_queue_remove_24dp),
                      contentDescription = "Remove from queue",
                      colorFilter = ColorFilter.tint(textMutedColor),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }

                Icon(
                  painter = painterResource(R.drawable.ic_drag_handle_24dp),
                  contentDescription = "Reorder queue item",
                  tint = if (isCurrent) accent else textMutedColor,
                  modifier = Modifier
                    .size(36.dp)
                    .padding(6.dp)
                    .draggableHandle()
                )
              }
            }
          }
        }
      }

      LaunchedEffect(visible) {
        if (visible) {
          val index = songs.indexOfFirst { it.id == playbackState.song.id }
          if (index != -1) {
            lazyState.scrollToItem(index)
          }
        }
      }
    }
  }
}

@Composable
private fun RowScope.ControlButton(
  onClick: () -> Unit,
  buttonSize: Dp? = null,
  onLongPressStart: (() -> Unit)? = null,
  onLongPressEnd: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val onClick by rememberUpdatedState(onClick)
  val onLongPressStart by rememberUpdatedState(onLongPressStart)
  val onLongPressEnd by rememberUpdatedState(onLongPressEnd)
  var longPressActive by remember { mutableStateOf(false) }

  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collect { interaction ->
      if ((interaction is PressInteraction.Release || interaction is PressInteraction.Cancel) &&
        longPressActive
      ) {
        longPressActive = false
        onLongPressEnd?.invoke()
      }
    }
  }

  val modifier = if (onLongPressStart != null && onLongPressEnd != null) {
    Modifier
      .clip(CircleShape)
      .combinedClickable(
        interactionSource = interactionSource,
        indication = ripple(color = LocalTheme.current.ripple),
        onClick = { onClick() },
        onLongClick = {
          longPressActive = true
          onLongPressStart?.invoke()
        }
      )
  } else {
    Modifier.clickWithRipple { onClick() }
  }
  val isPortrait = LocalContext.current.isPortraitOrientation()
  val buttonModifier = if (buttonSize != null) {
    modifier
      .weight(1f, isPortrait)
      .size(buttonSize)
  } else {
    modifier
      .weight(1f, isPortrait)
      .aspectRatio(1f)
  }

  Box(
    modifier = buttonModifier,
    contentAlignment = Alignment.Center
  ) {
    content()
  }
}
