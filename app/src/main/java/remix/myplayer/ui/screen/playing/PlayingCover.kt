package remix.myplayer.ui.screen.playing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.glide.addBitmapListener
import remix.myplayer.service.Command
import remix.myplayer.service.playback.MusicStateSource
import androidx.compose.animation.core.keyframes
import kotlin.math.roundToInt
import remix.myplayer.ui.widget.library.PatternCover
import remix.myplayer.viewmodel.playbackViewModel
import remix.myplayer.viewmodel.settingViewModel
import timber.log.Timber

/**
 * 封面切换动画样式
 */
enum class PlayingCoverAnimationStyle(val prefValue: String) {
  /**
   * 经典样式：方向滑动 + 淡入淡出 + 轻微缩放
   */
  CLASSIC(SettingPrefs.COVER_ANIMATION_CLASSIC),

  /**
   * 视差推进：新旧封面使用不同滑动距离
   */
  PARALLAX_PUSH(SettingPrefs.COVER_ANIMATION_PARALLAX_PUSH),

  /**
   * 卡片挤压：通过水平压缩/展开
   */
  CARD_SQUEEZE(SettingPrefs.COVER_ANIMATION_CARD_SQUEEZE),

  /**
   * 翻页：沿垂直轴做左右翻页效果
   */
  PAGE_TURN(SettingPrefs.COVER_ANIMATION_PAGE_TURN),

  /**
   * 分片错峰：封面分片按错峰节奏位移切换
   */
  SLICE_STAGGER(SettingPrefs.COVER_ANIMATION_SLICE_STAGGER),

  /**
   * 溶解缩放：弱位移 + 柔和淡入淡出
   */
  DISSOLVE_ZOOM(SettingPrefs.COVER_ANIMATION_DISSOLVE_ZOOM);

  companion object {
    private val selectableStyles = listOf(
      CLASSIC,
      PARALLAX_PUSH,
      CARD_SQUEEZE,
      PAGE_TURN,
      SLICE_STAGGER,
      DISSOLVE_ZOOM
    )

    fun fromPrefValue(value: String): PlayingCoverAnimationStyle {
      return selectableStyles.firstOrNull { it.prefValue == value } ?: CLASSIC
    }
  }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun PlayingCover(modifier: Modifier, song: Song) {
  val playbackVM = playbackViewModel
  val playbackUiState by playbackVM.playbackUiState.collectAsStateWithLifecycle()
  val settingState by settingViewModel.settingsState.collectAsStateWithLifecycle()
  val coverAnimationStyle = settingState.cover.coverAnimationStyle
  val coverAnimationSpeed = normalizeCoverAnimationSpeed(settingState.cover.coverAnimationSpeed)
  val isPlaying = playbackUiState.isPlaying
  val beatAnimationEnabled = settingState.cover.beatAnimationEnabled

  val playbackSpeed = settingState.play.speed.toFloatOrNull()?.coerceIn(0.5f, 2.5f) ?: 1.0f
  val basePeriod = (480f / playbackSpeed).roundToInt().coerceIn(240, 1000)

  val infiniteTransition = rememberInfiniteTransition(label = "CoverBeatTransition")

  // Punchy musical kick pulse with syncopated sub-bounce
  val beatScale by if (isPlaying && beatAnimationEnabled) {
    infiniteTransition.animateFloat(
      initialValue = 1.0f,
      targetValue = 1.052f,
      animationSpec = infiniteRepeatable(
        animation = keyframes {
          durationMillis = basePeriod
          1.0f at 0
          1.052f at (basePeriod * 0.22f).roundToInt() using FastOutSlowInEasing
          1.012f at (basePeriod * 0.55f).roundToInt()
          1.028f at (basePeriod * 0.72f).roundToInt()
          1.0f at basePeriod
        },
        repeatMode = RepeatMode.Restart
      ),
      label = "BeatScale"
    )
  } else {
    remember { mutableFloatStateOf(1.0f) }
  }

  // Fluid wave sway / rhythmic rotational tilt
  val waveRotation by if (isPlaying && beatAnimationEnabled) {
    infiniteTransition.animateFloat(
      initialValue = -1.6f,
      targetValue = 1.6f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = basePeriod * 2, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "WaveRotation"
    )
  } else {
    remember { mutableFloatStateOf(0f) }
  }

  // Fluid vertical wave bobbing
  val waveOffsetY by if (isPlaying && beatAnimationEnabled) {
    infiniteTransition.animateFloat(
      initialValue = -3.5f,
      targetValue = 3.5f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = (basePeriod * 1.5f).roundToInt(), easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "WaveOffsetY"
    )
  } else {
    remember { mutableFloatStateOf(0f) }
  }

  Timber.v("coverAnimationStyle: $coverAnimationStyle")
  val isPrevious = MusicStateSource.currentPlaybackUiState.lastOp == Command.SKIP_TO_PREVIOUS

  AnimatedContent(
    targetState = song,
    modifier = modifier,
    transitionSpec = {
      buildCoverContentTransform(
        style = coverAnimationStyle,
        isPrevious = isPrevious,
        speed = coverAnimationSpeed
      )
    },
    label = "CoverAnimation"
  ) { currentSong ->
    val beatModifier = if (isPlaying && beatAnimationEnabled) {
      Modifier.graphicsLayer {
        scaleX = beatScale
        scaleY = beatScale
        rotationZ = waveRotation
        translationY = waveOffsetY * density
      }
    } else {
      Modifier
    }

    val imageModifier = Modifier
      .fillMaxSize()
      .then(beatModifier)
      .clip(RoundedCornerShape(20.dp))

    val contentModifier = when (coverAnimationStyle) {
      PlayingCoverAnimationStyle.PAGE_TURN -> {
        horizontalFlip3DModifier(
          modifier = imageModifier,
          isTargetContent = currentSong.id == song.id,
          isPrevious = isPrevious,
          speed = coverAnimationSpeed
        )
      }

      PlayingCoverAnimationStyle.SLICE_STAGGER -> {
        sliceStaggerModifier(
          modifier = imageModifier,
          isTargetContent = currentSong.id == song.id,
          isPrevious = isPrevious,
          speed = coverAnimationSpeed
        )
      }

      else -> imageModifier
    }

    Box(modifier = contentModifier) {
      PatternCover(
        seed = currentSong.id,
        modifier = Modifier.fillMaxSize()
      )
      GlideImage(
        model = currentSong,
        contentDescription = "PlayingCover",
        contentScale = ContentScale.Crop,
        failure = placeholder(R.drawable.transparent_placeholder),
        loading = placeholder(R.drawable.transparent_placeholder),
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(20.dp))
      ) { builder ->
        builder.addBitmapListener { bitmap ->
          playbackVM.updateSwatch(bitmap)
        }
      }
    }
  }
}
