package remix.myplayer.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.widget.Toast
import dagger.hilt.android.EntryPointAccessors
import remix.myplayer.App
import remix.myplayer.R
import remix.myplayer.data.prefs.SettingPrefsEntryPoint
import remix.myplayer.data.prefs.delegate
import remix.myplayer.service.MusicServiceRemote
import timber.log.Timber
import kotlin.math.roundToInt

/**
 * created by Remix on 2019-05-06
 */
object EQHelper {
  private val settingPrefs = EntryPointAccessors.fromApplication(
    App.context,
    SettingPrefsEntryPoint::class.java
  ).settingPrefs()

  private var equalizer: Equalizer? = null
  private var bassBoost: BassBoost? = null
  private var virtualizer: Virtualizer? = null
  private var presetReverb: PresetReverb? = null

  private val bandLevels = ArrayList<Short>()

  /** Android PresetReverb presets exposed in the UI (index matches stored preference). */
  val echoPresets: List<Short> = listOf(
    PresetReverb.PRESET_NONE,
    PresetReverb.PRESET_SMALLROOM,
    PresetReverb.PRESET_MEDIUMROOM,
    PresetReverb.PRESET_LARGEROOM,
    PresetReverb.PRESET_MEDIUMHALL,
    PresetReverb.PRESET_LARGEHALL,
    PresetReverb.PRESET_PLATE,
  )

  var bandNumber: Short = 0
  var maxLevel: Short = 0
  var minLevel: Short = 0

  var enable = false

  //  var systemSessionOpen = false
//  var builtIdSessionOpen = false
  var builtEqualizerInit = false
  private var currentAudioSessionId: Int = AudioManager.AUDIO_SESSION_ID_GENERATE

  val isBassBoostEnabled: Boolean
    get() = enable && bassBoost?.strengthSupported == true

  var bassBoostStrength: Int
    get() = settingPrefs.bassBoostStrength
    set(strength) {
      settingPrefs.bassBoostStrength = strength
      if (isBassBoostEnabled) {
        tryRun({
          bassBoost?.setStrength(strength.toShort())
        }, {
          releaseBassBoost()
        })
      }
    }

  val isVirtualizerSupported: Boolean
    get() = virtualizer?.strengthSupported == true

  val isVirtualizerEnabled: Boolean
    get() = enable && isVirtualizerSupported

  var virtualizerStrength: Int
    get() = settingPrefs.virtualizerStrength
    set(strength) {
      settingPrefs.virtualizerStrength = strength
      if (isVirtualizerEnabled) {
        tryRun({
          virtualizer?.setStrength(strength.toShort())
        }, {
          releaseVirtualizer()
        })
      }
    }

  val enableEcho: Boolean
    get() = settingPrefs.enableEcho

  val echoPreset: Int
    get() = settingPrefs.echoPreset.coerceIn(0, echoPresets.lastIndex)

  val isEchoActive: Boolean
    get() = enableEcho && echoPreset > 0 && presetReverb != null

  fun init(context: Context, sessionId: Int, force: Boolean = false): Boolean {
    Timber.v("init, audioSessionId: $sessionId")

    if (!isValidAudioSessionId(sessionId)) {
      return false
    }

    // Always use the in-app equalizer UI/engine (ignore system EQ panel).
    enable = settingPrefs.enableEq
    // 不需要初始化
    if (!enable && !force) {
      builtEqualizerInit = false
      return builtEqualizerInit
    }

    Timber.v("init start")

    tryRun({
      releaseEqualizer()
      equalizer = Equalizer(0, sessionId)
      equalizer?.also { equalizer ->
        equalizer.enabled = enable

        //得到当前Equalizer引擎所支持的控制频率的标签数目。
        bandNumber = equalizer.numberOfBands

        //最小范围
        minLevel = equalizer.bandLevelRange[0]
        //最大范围
        maxLevel = equalizer.bandLevelRange[1]

        //得到当前Equalizer引擎所支持的控制频率的标签数目。
        bandNumber = equalizer.numberOfBands

        //得到之前存储的每个频率的db值
        bandLevels.clear()
        for (i in 0 until bandNumber) {
          var bandLevel by settingPrefs.sp.delegate("band$i", 0)
          bandLevels.add(bandLevel.toShort())
        }

        Timber.v("init finish")

        builtEqualizerInit = true
      }
    }, {
      equalizer = null
      builtEqualizerInit = false
      Timber.v("init failed")
    })

    if (builtEqualizerInit) {
      tryRun({
        releaseBassBoost()
        bassBoost = BassBoost(0, sessionId)
        bassBoost?.also { bassBoost ->
          bassBoost.enabled = enable && bassBoost.strengthSupported
          if (bassBoost.enabled) {
            bassBoost.setStrength(bassBoostStrength.toShort())
          }
        }
      }, {
        releaseBassBoost()
      })

      tryRun({
        releaseVirtualizer()
        virtualizer = Virtualizer(0, sessionId)
        virtualizer?.also { virtualizer ->
          virtualizer.enabled = enable && virtualizer.strengthSupported
          if (virtualizer.enabled) {
            virtualizer.setStrength(virtualizerStrength.toShort())
          }
        }
      }, {
        releaseVirtualizer()
      })
    }

    applyEcho(context, sessionId)

    return builtEqualizerInit
  }

  fun open(context: Context, audioSessionId: Int) {
    Timber.v("open, audioSessionId: $audioSessionId")

    if (!isValidAudioSessionId(audioSessionId)) {
      return
    }

    Timber.v("open built-in")
    //是否启用音效设置
    enable = settingPrefs.enableEq
    if (!enable) {
      return
    }
    tryRun({
      //EQ
      equalizer?.release()

      equalizer = Equalizer(0, audioSessionId)
      equalizer?.also { equalizer ->
        equalizer.enabled = enable

        //得到之前存储的每个频率的db值
        for (i in 0 until bandNumber) {
          if (enable) {
            equalizer.setBandLevel(i.toShort(), bandLevels[i])
          }
        }
      }
    }, {
      releaseEqualizer()
    })

    tryRun({
      //低音增强
      bassBoost?.release()
      bassBoost = BassBoost(0, audioSessionId)
      bassBoost?.also { bassBoost ->
        bassBoost.enabled = enable && bassBoost.strengthSupported
        if (bassBoost.enabled) {
          bassBoost.setStrength(bassBoostStrength.toShort())
        }
      }
    }, {
      releaseBassBoost()
    })

    tryRun({
      virtualizer?.release()
      virtualizer = Virtualizer(0, audioSessionId)
      virtualizer?.also { virtualizer ->
        virtualizer.enabled = enable && virtualizer.strengthSupported
        if (virtualizer.enabled) {
          virtualizer.setStrength(virtualizerStrength.toShort())
        }
      }
    }, {
      releaseVirtualizer()
    })

    applyEcho(context, audioSessionId)

    Timber.v("min: $minLevel max: $maxLevel bandNumber: $bandNumber")
  }

  fun close(context: Context, audioSessionId: Int) {
    Timber.v("close")

    if (!isValidAudioSessionId(audioSessionId)) {
      return
    }

    tryRun({
      releaseEqualizer()
    }, {

    })

    tryRun({
      releaseBassBoost()
    }, {

    })

    tryRun({
      releaseVirtualizer()
    }, {

    })

    tryRun({
      releaseEcho()
    }, {

    })

    closeSystemAudioEffectSession(context, audioSessionId)
  }

  fun updateAudioSession(context: Context, audioSessionId: Int) {
    if (!isValidAudioSessionId(audioSessionId)) {
      return
    }
    if (currentAudioSessionId == audioSessionId) {
      applyEcho(context, audioSessionId)
      return
    }

    if (isValidAudioSessionId(currentAudioSessionId)) {
      close(context, currentAudioSessionId)
    }

    Timber.v("updateAudioSession, from: $currentAudioSessionId to: $audioSessionId")
    currentAudioSessionId = audioSessionId
    init(context, audioSessionId)
    open(context, audioSessionId)
    // Echo is independent of the EQ master switch.
    applyEcho(context, audioSessionId)
  }

  /**
   * Applies or releases session echo (PresetReverb) independently of the EQ master switch.
   */
  fun applyEcho(context: Context, audioSessionId: Int) {
    if (!isValidAudioSessionId(audioSessionId)) {
      releaseEcho()
      return
    }
    currentAudioSessionId = audioSessionId
    if (!enableEcho || echoPreset <= 0) {
      releaseEcho()
      return
    }
    tryRun({
      releaseEcho()
      presetReverb = PresetReverb(0, audioSessionId).also { reverb ->
        reverb.preset = echoPresets[echoPreset]
        reverb.enabled = true
      }
    }, {
      releaseEcho()
    })
  }

  fun releaseCurrentAudioSession(context: Context) {
    close(context, currentAudioSessionId)
    currentAudioSessionId = AudioManager.AUDIO_SESSION_ID_GENERATE
  }


  fun getCenterFreq(band: Int): Int {
    return equalizer?.getCenterFreq(band.toShort()) ?: 0
  }

  fun getBandLevel(band: Int): Short {
    return bandLevels[band]
  }

  fun setBandLevel(band: Int, level: Int) {
    tryRun({
      equalizer?.setBandLevel(band.toShort(), level.toShort())
    }, {
      releaseEqualizer()
    })
    bandLevels[band] = level.toShort()
    var bandLevel by settingPrefs.sp.delegate("band${band}", 0)
    bandLevel = level
  }

  fun updateEnable(enable: Boolean) {
    this.enable = enable
    settingPrefs.enableEq = enable

    tryRun({
      equalizer?.enabled = enable
      for (band in 0 until bandNumber) {
        val bandLevel = if (enable) getBandLevel(band) else 0
        equalizer?.setBandLevel(band.toShort(), bandLevel)
      }
    }, {
      releaseEqualizer()
    })

    tryRun({
      bassBoost?.enabled = isBassBoostEnabled
      bassBoost?.setStrength(if (isBassBoostEnabled) bassBoostStrength.toShort() else 0)
    }, {
      releaseBassBoost()
    })

    tryRun({
      virtualizer?.enabled = isVirtualizerEnabled
      virtualizer?.setStrength(if (isVirtualizerEnabled) virtualizerStrength.toShort() else 0)
    }, {
      releaseVirtualizer()
    })
  }

  private fun releaseBassBoost() {
    bassBoost?.release()
    bassBoost = null
  }

  private fun releaseVirtualizer() {
    virtualizer?.release()
    virtualizer = null
  }

  private fun releaseEqualizer() {
    equalizer?.release()
    equalizer = null
  }

  fun reset() {
    for (i in 0 until bandNumber) {
      setBandLevel(i, 0)
    }

    bassBoostStrength = 0
    virtualizerStrength = 0
  }

  private fun releaseEcho() {
    try {
      presetReverb?.enabled = false
      presetReverb?.release()
    } catch (e: Exception) {
      Timber.w(e)
    }
    presetReverb = null
  }

  /** Built-in Android Equalizer presets, if the device exposes any. */
  fun systemPresetNames(): List<String> {
    val eq = equalizer ?: return emptyList()
    return try {
      (0 until eq.numberOfPresets).map { index ->
        eq.getPresetName(index.toShort()).orEmpty().ifBlank { "Preset ${index + 1}" }
      }
    } catch (e: Exception) {
      Timber.w(e)
      emptyList()
    }
  }

  fun useSystemPreset(index: Int): Boolean {
    val eq = equalizer ?: return false
    return tryRun({
      eq.usePreset(index.toShort())
      syncBandLevelsFromEqualizer()
    }, {
      releaseEqualizer()
    })
  }

  /**
   * Applies a software preset defined as relative gains in [-1, 1] across bands
   * (interpolated when the device band count differs from the preset length).
   */
  fun applyRelativeGains(relativeGains: FloatArray) {
    if (bandNumber <= 0) return
    val gains = interpolateRelativeGains(relativeGains, bandNumber.toInt())
    for (i in 0 until bandNumber) {
      val level = (minLevel + (maxLevel - minLevel) * ((gains[i] + 1f) / 2f)).roundToInt()
        .coerceIn(minLevel.toInt(), maxLevel.toInt())
      setBandLevel(i, level)
    }
  }

  private fun syncBandLevelsFromEqualizer() {
    val eq = equalizer ?: return
    for (i in 0 until bandNumber) {
      val level = eq.getBandLevel(i.toShort())
      bandLevels[i] = level
      var bandLevel by settingPrefs.sp.delegate("band$i", 0)
      bandLevel = level.toInt()
    }
  }

  private fun interpolateRelativeGains(source: FloatArray, targetBands: Int): FloatArray {
    if (source.isEmpty() || targetBands <= 0) {
      return FloatArray(targetBands.coerceAtLeast(0))
    }
    if (source.size == targetBands) return source.copyOf()
    if (targetBands == 1) return floatArrayOf(source.average().toFloat())

    val result = FloatArray(targetBands)
    val lastSrc = source.lastIndex.toFloat()
    for (i in 0 until targetBands) {
      val pos = i.toFloat() / (targetBands - 1) * lastSrc
      val left = pos.toInt().coerceIn(0, source.lastIndex)
      val right = (left + 1).coerceAtMost(source.lastIndex)
      val t = pos - left
      result[i] = source[left] * (1f - t) + source[right] * t
    }
    return result
  }

  private fun openSystemAudioEffectSession(context: Context, audioSessionId: Int) {
    val audioEffectsIntent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
    audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
    audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
    context.sendBroadcast(audioEffectsIntent)
  }

  private fun closeSystemAudioEffectSession(context: Context, audioSessionId: Int) {
    val audioEffectsIntent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
    audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
    audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
    context.sendBroadcast(audioEffectsIntent)
  }

  /**
   * 启动均衡器
   */
  @JvmStatic
  fun startEqualizer(activity: Activity, navigateToEq: () -> Unit) {
    val sessionId = MusicServiceRemote.getAudioSessionId() ?: return
    if (!isValidAudioSessionId(sessionId)) {
      Toast.makeText(
        activity,
        activity.resources.getString(R.string.no_audio_ID),
        Toast.LENGTH_LONG
      ).show()
      return
    }
    // Always open the in-app equalizer (preset chips + thumb-zone bands).
    navigateToEq()
  }

  private fun isValidAudioSessionId(sessionId: Int): Boolean {
    return sessionId != AudioEffect.ERROR_BAD_VALUE &&
      sessionId != AudioManager.AUDIO_SESSION_ID_GENERATE
  }

  private fun tryRun(block: () -> Unit, error: () -> Unit): Boolean {
    return try {
      block()
      true
    } catch (e: Exception) {
      Timber.w(e)
      error()
      false
    }
  }

  const val REQUEST_EQ = 0
}
