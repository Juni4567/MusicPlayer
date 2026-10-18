package remix.myplayer.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import androidx.annotation.FloatRange

/**
 * Created by Remix on 2018/3/13.
 *
 * Player volume may exceed 1.0 when [volume boost][remix.myplayer.data.prefs.SettingPrefs.volumeBoost]
 * is enabled (up to 200% / 2.0).
 */
class VolumeController(private val service: MusicService) {

  private val handler = Handler(Looper.getMainLooper())
  private var animator: ValueAnimator? = null
  private var lastVolume = 1f

  /** Current full-scale volume (1.0–2.0 from prefs). */
  fun fullVolume(): Float = service.settingPrefs.volumeBoostValue

  fun restoreFullVolume() {
    directTo(fullVolume())
  }

  fun directTo(@FloatRange(from = 0.0, to = 2.0) toVolume: Float) {
    directTo(toVolume, toVolume)
  }

  private fun directTo(
    @FloatRange(from = 0.0, to = 2.0) leftVolume: Float,
    @FloatRange(from = 0.0, to = 2.0) rightVolume: Float
  ) {
    val volume = leftVolume.coerceIn(0f, VOLUME_CEILING)
    cancelAnimator()
    service.playback.setVolume(volume)
    lastVolume = volume
  }

  /**
   * 淡入
   */
  fun fadeIn() {
    fadeTo(fullVolume())
  }

  /**
   * 淡出
   */
  fun fadeOut() {
    fadeTo(0f, onEnd = { service.playback.pause() })
  }

  private fun fadeTo(
    @FloatRange(from = 0.0, to = 2.0) targetVolume: Float,
    onEnd: (() -> Unit)? = null
  ) {
    val target = targetVolume.coerceIn(0f, VOLUME_CEILING)
    handler.post {
      cancelAnimator()
      animator = ValueAnimator.ofFloat(lastVolume, target).apply {
        duration = DURATION_IN_MS
        addUpdateListener {
          val volume = it.animatedValue as Float
          service.playback.setVolume(volume)
          lastVolume = volume
        }
        addListener(object : AnimatorListenerAdapter() {
          private var canceled = false

          override fun onAnimationCancel(animation: Animator) {
            canceled = true
          }

          override fun onAnimationEnd(animation: Animator) {
            if (animator === animation) {
              animator = null
            }
            if (!canceled) {
              onEnd?.invoke()
            }
          }
        })
      }
      animator?.start()
    }
  }

  private fun cancelAnimator() {
    animator?.cancel()
    animator = null
  }

  companion object {

    private const val DURATION_IN_MS = 400L
    private const val VOLUME_CEILING = 2.0f
  }
}
