package remix.myplayer.ui.screen.playing

import android.content.Intent
import androidx.compose.foundation.gestures.detectDragGestures
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
 * - swipe up    → lyrics
 * - swipe down  → back to cover (when lyrics visible)
 */
fun Modifier.playingCoverGestures(
  showingLyrics: Boolean,
  onShowLyrics: () -> Unit,
  onHideLyrics: () -> Unit,
): Modifier = pointerInput(showingLyrics) {
  val threshold = 80f
  var totalX = 0f
  var totalY = 0f

  detectDragGestures(
    onDragStart = {
      totalX = 0f
      totalY = 0f
    },
    onDrag = { change, dragAmount ->
      change.consume()
      totalX += dragAmount.x
      totalY += dragAmount.y
    },
    onDragEnd = {
      val horizontal = abs(totalX) > abs(totalY) && abs(totalX) > threshold
      val vertical = abs(totalY) > abs(totalX) && abs(totalY) > threshold

      when {
        showingLyrics && vertical && totalY > 0 -> onHideLyrics()
        !showingLyrics && vertical && totalY < 0 -> onShowLyrics()
        !showingLyrics && horizontal && totalX < 0 -> {
          sendLocalBroadcast(
            Intent(MusicService.ACTION_CMD).putExtra(
              MusicService.EXTRA_COMMAND,
              Command.SKIP_TO_NEXT
            )
          )
        }
        !showingLyrics && horizontal && totalX > 0 -> {
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
