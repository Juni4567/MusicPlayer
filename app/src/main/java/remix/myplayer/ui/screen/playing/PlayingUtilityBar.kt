package remix.myplayer.ui.screen.playing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import remix.myplayer.ui.widget.library.GlideCover
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.helper.VolumeBoostHudController
import remix.myplayer.service.MusicServiceRemote
import remix.myplayer.viewmodel.settingViewModel
import androidx.core.graphics.toColorInt
import androidx.palette.graphics.Palette
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import remix.myplayer.R
import remix.myplayer.data.prefs.SettingPrefs.Companion.BOTTOM_SHOW_BOTH
import remix.myplayer.data.prefs.SettingPrefs.Companion.BOTTOM_SHOW_NEXT
import remix.myplayer.data.prefs.SettingPrefs.Companion.BOTTOM_SHOW_NONE
import remix.myplayer.data.prefs.SettingPrefs.Companion.BOTTOM_SHOW_VOLUME
import remix.myplayer.service.playback.PlaybackUiState
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.LineSlider
import remix.myplayer.ui.widget.common.defaultLineSliderProperties
import remix.myplayer.util.ext.CenterInBox
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.util.ext.clickableWithoutRipple

private const val ACTION_VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION"
private const val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
private const val EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE"

@Composable
internal fun PlayingUtilityBar(
  modifier: Modifier,
  playingScreenBottom: Int,
  musicState: PlaybackUiState,
  swatch: Palette.Swatch
) {
  Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
    val swatchColor = Color(swatch.rgb)
    assert(playingScreenBottom != BOTTOM_SHOW_NONE)

    var nextSongIsVisible by remember(playingScreenBottom) {
      mutableStateOf(playingScreenBottom == BOTTOM_SHOW_NEXT)
    }
    var volumeIsVisible by remember(playingScreenBottom) {
      mutableStateOf(playingScreenBottom != BOTTOM_SHOW_NEXT)
    }

    var refreshKey by remember(playingScreenBottom) {
      mutableIntStateOf(0)
    }

    AnimatedVisibility(nextSongIsVisible, enter = fadeIn(), exit = fadeOut()) {
      NextSong(musicState, swatchColor) {
        if (playingScreenBottom != BOTTOM_SHOW_NEXT) {
          nextSongIsVisible = false
          volumeIsVisible = true
          refreshKey++
        }
      }
    }
    AnimatedVisibility(volumeIsVisible, enter = fadeIn(), exit = fadeOut()) {
      VolumeSeekbar(swatchColor) {
        if (playingScreenBottom != BOTTOM_SHOW_VOLUME) {
          refreshKey++
        }
      }
    }

    LaunchedEffect(refreshKey, playingScreenBottom) {
      if (refreshKey == 0 && playingScreenBottom == BOTTOM_SHOW_BOTH || refreshKey > 0) {
        // nextSong -> GONE
        // volume -> VISIBLE
        delay(3000)
        nextSongIsVisible = true
        volumeIsVisible = false
      }
    }
  }
}

@Composable
private fun NextSong(musicState: PlaybackUiState, swatchColor: Color, onClick: (() -> Unit)? = null) {
  val theme = LocalTheme.current
  val nextSong = musicState.nextSong
  val cleanTitle = nextSong.title.ifEmpty { stringResource(R.string.unknown_song) }
  val cleanArtist = nextSong.cleanArtist

  Row(
    modifier = Modifier
      .padding(horizontal = 24.dp)
      .fillMaxWidth()
      .height(42.dp)
      .clip(RoundedCornerShape(21.dp))
      .background(theme.surface2.copy(alpha = 0.85f))
      .border(0.8.dp, swatchColor.copy(alpha = 0.25f), RoundedCornerShape(21.dp))
      .clickWithRipple(circle = false) {
        onClick?.invoke()
      }
      .padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    GlideCover(
      model = nextSong,
      circle = true,
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
    )

    Spacer(modifier = Modifier.width(8.dp))

    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(6.dp))
        .background(swatchColor.copy(alpha = 0.16f))
        .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
      Text(
        text = "UP NEXT",
        color = swatchColor,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = cleanTitle,
        color = theme.textPrimary,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f, fill = false)
      )
      if (cleanArtist.isNotEmpty()) {
        Text(
          text = " · $cleanArtist",
          color = theme.textSecondary,
          fontSize = 11.5.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    Spacer(modifier = Modifier.width(4.dp))

    Image(
      painter = painterResource(R.drawable.ic_navigate_next_white_24dp),
      contentDescription = null,
      colorFilter = ColorFilter.tint(theme.textMuted),
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
private fun VolumeSeekbar(swatchColor: Color, onValueChange: () -> Unit) {
  val context = LocalContext.current
  val audioManager = remember {
    context.getSystemService(AUDIO_SERVICE) as AudioManager
  }
  val settingVM = settingViewModel
  val settingState by settingVM.settingsState.collectAsStateWithLifecycle()
  val volumeBoost = settingState.play.volumeBoost

  Row(
    horizontalArrangement = Arrangement.spacedBy(24.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(horizontal = 18.dp)
  ) {
    CenterInBox(
      Modifier
        .size(48.dp)
        .clickWithRipple {
          if (volumeBoost > 1.0f) {
            val newBoost = (volumeBoost - 0.05f).coerceAtLeast(1.0f)
            settingVM.setVolumeBoost(newBoost)
            MusicServiceRemote.service?.replayGainController?.processor?.setVolumeBoost(newBoost)
            VolumeBoostHudController.show(newBoost)
          } else {
            audioManager.adjustStreamVolume(
              STREAM_MUSIC,
              AudioManager.ADJUST_LOWER,
              AudioManager.FLAG_PLAY_SOUND
            )
          }
          onValueChange()
        }) {
      Image(
        painter = painterResource(R.drawable.ic_volume_down_black_24dp),
        contentDescription = "PlayingBottomBarVolumeDown",
        colorFilter = ColorFilter.tint(
          if (volumeBoost > 1.0f) Color(0xFFFF9800) else swatchColor.copy(0.6f)
        )
      )
    }

    VolumeSeekBar(audioManager, swatchColor, volumeBoost, onValueChange)

    CenterInBox(
      Modifier
        .size(48.dp)
        .clickWithRipple {
          val maxStream = audioManager.getStreamMaxVolume(STREAM_MUSIC)
          val currentStream = audioManager.getStreamVolume(STREAM_MUSIC)
          if (currentStream < maxStream) {
            audioManager.adjustStreamVolume(
              STREAM_MUSIC,
              AudioManager.ADJUST_RAISE,
              AudioManager.FLAG_PLAY_SOUND
            )
          } else {
            val newBoost = (volumeBoost + 0.05f).coerceAtMost(SettingPrefs.VOLUME_BOOST_MAX)
            settingVM.setVolumeBoost(newBoost)
            MusicServiceRemote.service?.replayGainController?.processor?.setVolumeBoost(newBoost)
            VolumeBoostHudController.show(newBoost)
          }
          onValueChange()
        }) {
      Image(
        painter = painterResource(R.drawable.ic_volume_up_black_24dp),
        contentDescription = "PlayingBottomBarVolumeUp",
        colorFilter = ColorFilter.tint(
          if (volumeBoost > 1.0f) Color(0xFFFF9800) else swatchColor.copy(0.6f)
        )
      )
    }
  }
}

@Composable
private fun RowScope.VolumeSeekBar(
  audioManager: AudioManager,
  swatchColor: Color,
  volumeBoost: Float,
  onValueChange: () -> Unit
) {
  val context = LocalContext.current
  val settingVM = settingViewModel
  var minStream by remember { mutableIntStateOf(0) }
  var maxStream by remember { mutableIntStateOf(1) }
  var currentStream by remember { mutableIntStateOf(0) }
  var isSliderDragging by remember { mutableStateOf(false) }

  val isBoosted = volumeBoost > 1.0f
  val currentCompositeValue = if (isBoosted) {
    100f + (volumeBoost - 1.0f) * 100f
  } else {
    if (maxStream > 0) (currentStream.toFloat() / maxStream) * 100f else 0f
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.weight(1f)
  ) {
    // Volume / Boost status label
    val statusText = if (isBoosted) {
      "🔥 BOOST ${(volumeBoost * 100f).roundToInt()}%"
    } else if (currentStream >= maxStream && maxStream > 0) {
      "100%"
    } else {
      "${currentCompositeValue.roundToInt()}%"
    }

    Text(
      text = statusText,
      fontSize = 11.sp,
      fontWeight = if (isBoosted) FontWeight.Bold else FontWeight.Medium,
      color = if (isBoosted) Color(0xFFFF9800) else swatchColor.copy(alpha = 0.75f),
      maxLines = 1
    )

    LineSlider(
      value = currentCompositeValue.coerceIn(0f, 200f),
      valueRange = 0f..200f,
      steps = 199,
      onValueChange = { targetVal ->
        isSliderDragging = true
        if (targetVal <= 100f) {
          if (volumeBoost > 1.0f) {
            settingVM.setVolumeBoost(1.0f)
            MusicServiceRemote.service?.replayGainController?.processor?.setVolumeBoost(1.0f)
          }
          val vol = if (maxStream > 0) {
            ((targetVal / 100f) * maxStream).roundToInt().coerceIn(minStream, maxStream)
          } else 0
          if (vol != currentStream) {
            currentStream = vol
            audioManager.setStreamVolume(STREAM_MUSIC, vol, 0)
          }
        } else {
          if (currentStream != maxStream) {
            currentStream = maxStream
            audioManager.setStreamVolume(STREAM_MUSIC, maxStream, 0)
          }
          val newBoost = (1.0f + (targetVal - 100f) / 100f).coerceIn(1.0f, SettingPrefs.VOLUME_BOOST_MAX)
          val snapped = SettingPrefs.normalizeVolumeBoost(newBoost)
          settingVM.setVolumeBoost(snapped)
          MusicServiceRemote.service?.replayGainController?.processor?.setVolumeBoost(snapped)
          VolumeBoostHudController.show(snapped)
        }
        onValueChange()
      },
      onValueChangeFinished = {
        isSliderDragging = false
        currentStream = audioManager.getStreamVolume(STREAM_MUSIC).coerceIn(minStream, maxStream)
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(32.dp),
      properties = defaultLineSliderProperties.copy(
        trackHeight = 3.dp,
        trackBackgroundColor = LocalTheme.current.control,
        trackProgressColor = if (isBoosted) Color(0xFFFF9800) else swatchColor,
        thumbColor = if (isBoosted) Color(0xFFFF9800) else swatchColor,
        thumbWidth = 4.dp,
        thumbHeight = 10.dp,
        thumbShape = RoundedCornerShape(2.dp)
      )
    )
  }

  LaunchedEffect(Unit) {
    minStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      audioManager.getStreamMinVolume(STREAM_MUSIC)
    } else 0
    maxStream = audioManager.getStreamMaxVolume(STREAM_MUSIC)
    currentStream = audioManager.getStreamVolume(STREAM_MUSIC).coerceIn(minStream, maxStream)
  }

  DisposableEffect(audioManager, context, minStream, maxStream) {
    val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_VOLUME_CHANGED) return
        if (intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1) != STREAM_MUSIC) return
        if (isSliderDragging) return

        val volume = intent.getIntExtra(
          EXTRA_VOLUME_STREAM_VALUE,
          audioManager.getStreamVolume(STREAM_MUSIC)
        )
        currentStream = volume.coerceIn(minStream, maxStream)
      }
    }
    val filter = IntentFilter(ACTION_VOLUME_CHANGED)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
      context.registerReceiver(receiver, filter)
    }

    onDispose {
      runCatching {
        context.unregisterReceiver(receiver)
      }
    }
  }
}
