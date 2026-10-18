package remix.myplayer.service.playback

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import remix.myplayer.data.prefs.SettingPrefs
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges echo preferences to the [EchoDelayAudioProcessor] in the ExoPlayer sink.
 */
@OptIn(UnstableApi::class)
@Singleton
class EchoController @Inject constructor(
  private val settingPrefs: SettingPrefs,
) {

  val processor = EchoDelayAudioProcessor()

  /** Apply current SharedPreferences echo settings to the DSP processor. */
  fun applyFromPrefs() {
    val enabled = settingPrefs.enableEcho && settingPrefs.echoPreset > 0
    if (!enabled) {
      processor.disable()
      return
    }
    val params = EchoPresets.paramsFor(settingPrefs.echoPreset)
    processor.setEffect(
      enabled = true,
      delayMs = params.delayMs,
      feedback = params.feedback,
      wet = params.wet,
      dry = params.dry,
    )
  }
}
