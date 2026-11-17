package remix.myplayer.service.notification

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.service.Command
import remix.myplayer.service.MusicService
import remix.myplayer.service.MusicService.Companion.EXTRA_COMMAND
import remix.myplayer.util.DensityUtil

/**
 * Created by Remix on 2017/11/22.
 */
@TargetApi(Build.VERSION_CODES.O)
class NotifyImpl24(context: MusicService) : Notify(context) {

  private val defaultBitmap =
    BitmapFactory.decodeResource(service.resources, R.drawable.album_empty_bg_night)
  private val size = DensityUtil.dip2px(service, 128f)
  private var lastBitmap: Bitmap? = null
  private var lastSongId: Long = -1L

  override fun updateAndNotify() {
    val song = playbackState.song

    if (song.id != lastSongId) {
      lastSongId = song.id
      lastBitmap = null
    }

    // Immediately post with current known bitmap to prevent title or cover flashing
    updateWithBitmap(lastBitmap ?: defaultBitmap, song)

    // Load high quality artwork via Glide
    Glide.with(service).clear(target)
    target = Glide.with(service)
      .asBitmap()
      .load(song)
      .centerCrop()
      .override(size, size)
      .into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          lastBitmap = resource
          updateWithBitmap(resource, song)
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
          updateWithBitmap(lastBitmap ?: defaultBitmap, song)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }

        override fun onLoadStarted(placeholder: Drawable?) {
          // Never pass null; keep previous or fallback bitmap so the player never loses cover
          updateWithBitmap(lastBitmap ?: defaultBitmap, song)
        }
      })
  }

  private fun updateWithBitmap(bitmap: Bitmap?, song: Song) {
    if (song.id != playbackState.song.id) {
      return
    }
    val playPauseIcon =
      if (playbackState.isPlaying) R.drawable.ic_pause_black_24dp else R.drawable.ic_play_arrow_black_24dp

    val title = song.showName.ifEmpty {
      song.title.ifEmpty { service.getString(R.string.unknown_song) }
    }
    val artistPart = song.cleanArtist.ifEmpty { song.artist }
    val artistAlbum = listOf(artistPart, song.album)
      .filter { it.isNotBlank() }
      .joinToString(" · ")
      .ifEmpty { service.getString(R.string.unknown_artist) }

    val appAccentColor = try {
      service.themeController.appTheme.secondary.toArgb()
    } catch (e: Exception) {
      0xFFE91E63.toInt()
    }

    val deleteIntent = Intent(MusicService.ACTION_CMD)
    deleteIntent.putExtra(EXTRA_COMMAND, Command.CLOSE_NOTIFY)

    val notification = NotificationCompat.Builder(service, PLAYING_NOTIFICATION_CHANNEL_ID)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setSmallIcon(R.drawable.icon_notifbar)
      .addAction(
        R.drawable.ic_skip_previous_black_24dp, service.getString(R.string.previous),
        buildPendingIntent(service, Command.SKIP_TO_PREVIOUS)
      )
      .addAction(
        playPauseIcon, service.getString(R.string.play_pause),
        buildPendingIntent(service, Command.PLAY_PAUSE)
      )
      .addAction(
        R.drawable.ic_skip_next_black_24dp, service.getString(R.string.next),
        buildPendingIntent(service, Command.SKIP_TO_NEXT)
      )
      .addAction(
        R.drawable.ic_close_white_24dp, service.getString(R.string.close),
        buildPendingIntent(service, Command.CLOSE_NOTIFY)
      )
      .setDeleteIntent(buildPendingIntent(service, Command.CLOSE_NOTIFY))
      .setContentIntent(contentIntent)
      .setContentTitle(title)
      .setContentText(artistAlbum)
      .setLargeIcon(bitmap ?: lastBitmap ?: defaultBitmap)
      .setShowWhen(false)
      .setOngoing(playbackState.isPlaying)
      .setPriority(PRIORITY_MAX)
      .setColor(appAccentColor)
      .setColorized(true)
      .setStyle(
        androidx.media.app.NotificationCompat.MediaStyle()
          .setShowActionsInCompactView(0, 1, 2)
          .setMediaSession(service.mediaSession.sessionToken)
      )
      .build()

    startForegroundOrNotify(notification)
  }
}
