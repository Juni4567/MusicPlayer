package remix.myplayer.ui.screen.playing

import android.content.Intent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import remix.myplayer.service.Command
import remix.myplayer.service.MusicService
import remix.myplayer.util.Util.sendLocalBroadcast
import kotlin.math.abs

/**
 * Cover gestures:
 * - swipe left  → next track
 * - swipe right → previous track
 * - vertical drags flow through to the parent's anchoredDraggable for seamless downward dismiss
 */
fun Modifier.playingCoverGestures(
  showingLyrics: Boolean = false,
  @Suppress("UNUSED_PARAMETER") onShowLyrics: () -> Unit = {},
  @Suppress("UNUSED_PARAMETER") onHideLyrics: () -> Unit = {},
  @Suppress("UNUSED_PARAMETER") onMinimize: () -> Unit = {},
): Modifier = pointerInput(showingLyrics) {
  if (showingLyrics) {
    return@pointerInput
  }
  val threshold = 60f
  var totalX = 0f

  detectHorizontalDragGestures(
    onDragStart = {
      totalX = 0f
    },
    onHorizontalDrag = { change, dragAmount ->
      change.consume()
      totalX += dragAmount
    },
    onDragEnd = {
      if (abs(totalX) > threshold) {
        if (totalX < 0) {
          sendLocalBroadcast(
            Intent(MusicService.ACTION_CMD).putExtra(
              MusicService.EXTRA_COMMAND,
              Command.SKIP_TO_NEXT
            )
          )
        } else {
          sendLocalBroadcast(
            Intent(MusicService.ACTION_CMD).putExtra(
              MusicService.EXTRA_COMMAND,
              Command.SKIP_TO_PREVIOUS
            )
          )
        }
      }
    }
  )
}
