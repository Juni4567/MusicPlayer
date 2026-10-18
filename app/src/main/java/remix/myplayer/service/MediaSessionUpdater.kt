package remix.myplayer.service

import android.util.Log

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.data.model.audio.Song.Companion.EMPTY_SONG
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.data.prefs.SettingPrefs.Companion.LOCKSCREEN_CLOSE
import remix.myplayer.service.playback.Playback
import remix.myplayer.ui.nav.MessageNotifier
import remix.myplayer.ui.theme.ThemeController
import remix.myplayer.util.ext.tryLaunch
import timber.log.Timber
import javax.inject.Inject

/** Snapshot of non-playback state shown as MediaSession custom actions. */
data class MediaSessionExtras(
  val isFavorite: Boolean,
  val playMode: Int,
  val desktopLyricEnabled: Boolean,
  val desktopLyricLocked: Boolean,
  val timerRunning: Boolean,
)

class MediaSessionUpdater @Inject constructor(
  private val themeController: ThemeController
) {

  fun clear(mediaSession: MediaSessionCompat) {
    mediaSession.setMetadata(MediaMetadataCompat.Builder().build())
    mediaSession.setPlaybackState(
      PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_NONE, 0, 1f).build()
    )
  }

  fun updateMediaSessionQueue(
    scope: CoroutineScope,
    mediaSession: MediaSessionCompat,
    playlist: List<Song>,
    title: String?
  ) {
    Timber.v("updateQueueItem")
    scope.tryLaunch(block = {
      val queue = withContext(Dispatchers.Default) {
        ArrayList(playlist)
          .map { song ->
            return@map MediaSessionCompat.QueueItem(
              MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id.toString())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                .build().description, song.id
            )
          }
      }
      Timber.v("updateQueueItem, queue: ${queue.size}")
      mediaSession.setQueueTitle(title)
      mediaSession.setQueue(queue)
    }, catch = {
      MessageNotifier.show(it.toString())
      Timber.w(it)
    })
  }

  fun updateMetadata(
    context: Context,
    mediaSession: MediaSessionCompat,
    playback: Playback,
    lockScreen: Int,
    extras: MediaSessionExtras,
  ) {
    val currentSong = playback.currentSong ?: EMPTY_SONG
    if (currentSong == EMPTY_SONG) {
      return
    }

    val builder = MediaMetadataCompat.Builder()
      .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, currentSong.id.toString())
      .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong.album)
      .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong.artist)
      .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, currentSong.artist)
      .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentSong.duration)
      .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, (playback.currentIndex + 1).toLong())
      .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.title)
      .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, currentSong.title)
      .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, currentSong.artist)
      .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, playback.itemCount.toLong())

    mediaSession.setMetadata(builder.build())
    updatePlaybackState(context, mediaSession, playback, extras)

    val shouldLoadAlbumArt =
      lockScreen != LOCKSCREEN_CLOSE || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    if (!shouldLoadAlbumArt) {
      return
    }

    val placeholder =
      if (themeController.appTheme.isLight) R.drawable.album_empty_bg_day else R.drawable.album_empty_bg_night
    Glide.with(context)
      .asBitmap()
      .load(currentSong)
      .error(placeholder)
      .centerCrop()
      .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
      .into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          setMediaSessionData(resource)
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
          setMediaSessionData((errorDrawable as? BitmapDrawable)?.bitmap)
        }

        private fun setMediaSessionData(result: Bitmap?) {
          builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, copy(result))
          mediaSession.setMetadata(builder.build())
          updatePlaybackState(context, mediaSession, playback, extras)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
      })
  }

  fun updatePlaybackState(
    context: Context,
    mediaSession: MediaSessionCompat,
    playback: Playback,
    extras: MediaSessionExtras,
  ) {
    val builder = PlaybackStateCompat.Builder()
    builder.setActiveQueueItemId(playback.currentSong?.id ?: return)
      .setState(
        if (playback.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
        playback.position,
        playback.speed
      )
      .setActions(MEDIA_SESSION_ACTIONS)

    // Volume / media-output panel custom actions (Android 13+). Keep labels short.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      builder.addCustomAction(
        PlaybackStateCompat.CustomAction.Builder(
          MusicService.ACTION_LOVE,
          context.getString(
            if (extras.isFavorite) R.string.media_action_unfavorite else R.string.media_action_favorite
          ),
          if (extras.isFavorite) R.drawable.widget_btn_like_prs else R.drawable.widget_btn_like_nor
        ).build()
      )
      builder.addCustomAction(
        PlaybackStateCompat.CustomAction.Builder(
          MusicService.ACTION_CHANGE_MODE,
          context.getString(modeLabelRes(extras.playMode)),
          modeIconRes(extras.playMode)
        ).build()
      )
      builder.addCustomAction(
        PlaybackStateCompat.CustomAction.Builder(
          MusicService.ACTION_TOGGLE_TIMER,
          context.getString(
            if (extras.timerRunning) R.string.cancel_timer else R.string.start_timer
          ),
          if (extras.timerRunning) R.drawable.ic_timer_on_24dp else R.drawable.ic_player_timer_24dp
        ).build()
      )
      builder.addCustomAction(
        PlaybackStateCompat.CustomAction.Builder(
          if (extras.desktopLyricLocked) {
            MusicService.ACTION_UNLOCK_DESKTOP_LYRIC
          } else {
            MusicService.ACTION_TOGGLE_DESKTOP_LYRIC
          },
          context.getString(
            when {
              extras.desktopLyricLocked -> R.string.media_action_unlock_lyrics
              extras.desktopLyricEnabled -> R.string.close_desktop_lyrics
              else -> R.string.media_action_desktop_lyrics
            }
          ),
          if (extras.desktopLyricEnabled || extras.desktopLyricLocked) {
            R.drawable.icon_notify_desktop_lyric_unlock
          } else {
            R.drawable.icon_notify_lyric
          }
        ).build()
      )
    }
    mediaSession.setPlaybackState(builder.build())
  }

  private fun modeLabelRes(playMode: Int): Int = when (playMode) {
    SettingPrefs.MODE_SHUFFLE -> R.string.model_random
    SettingPrefs.MODE_REPEAT -> R.string.model_repeat
    else -> R.string.model_normal
  }

  private fun modeIconRes(playMode: Int): Int = when (playMode) {
    SettingPrefs.MODE_SHUFFLE -> R.drawable.play_btn_shuffle
    SettingPrefs.MODE_REPEAT -> R.drawable.play_btn_loop_one
    else -> R.drawable.play_btn_loop
  }

  private fun copy(bitmap: Bitmap?): Bitmap? {
    if (bitmap == null || bitmap.isRecycled) {
      return null
    }
    val config: Bitmap.Config = bitmap.config ?: return null
    return try {
      bitmap.copy(config, false)
    } catch (e: OutOfMemoryError) {
      Log.e("APlayer", "Error", e)
      null
    }
  }

  private companion object {
    private const val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
        or PlaybackStateCompat.ACTION_PAUSE
        or PlaybackStateCompat.ACTION_PLAY_PAUSE
        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        or PlaybackStateCompat.ACTION_STOP
        or PlaybackStateCompat.ACTION_SEEK_TO)
  }
}
