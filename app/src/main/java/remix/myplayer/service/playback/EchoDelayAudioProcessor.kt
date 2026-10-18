package remix.myplayer.service.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Simple feedback delay (echo) applied in the PCM domain.
 *
 * Works for both 16-bit and float PCM. When [enabled] is false the processor
 * is a pass-through so there is no CPU cost from the delay line.
 */
@OptIn(UnstableApi::class)
class EchoDelayAudioProcessor : BaseAudioProcessor() {

  @Volatile
  private var enabled = false

  @Volatile
  private var delayMs = 200f

  @Volatile
  private var feedback = 0.35f

  @Volatile
  private var wet = 0.4f

  @Volatile
  private var dry = 0.85f

  private var delayBuffer: FloatArray = FloatArray(0)
  private var writeFrame = 0
  private var delayFrames = 0
  private var channelCount = 0

  /**
   * Configure the echo effect.
   *
   * @param enabled whether wet signal is mixed in
   * @param delayMs delay time in milliseconds (clamped)
   * @param feedback regeneration amount 0..0.85
   * @param wet wet mix 0..1
   * @param dry dry mix 0..1
   */
  fun setEffect(
    enabled: Boolean,
    delayMs: Float = this.delayMs,
    feedback: Float = this.feedback,
    wet: Float = this.wet,
    dry: Float = this.dry,
  ) {
    this.enabled = enabled
    this.delayMs = delayMs.coerceIn(MIN_DELAY_MS, MAX_DELAY_MS)
    this.feedback = feedback.coerceIn(0f, MAX_FEEDBACK)
    this.wet = wet.coerceIn(0f, 1f)
    this.dry = dry.coerceIn(0f, 1f)
    // Delay length may change while configured — rebuild on next process if needed.
    if (outputAudioFormat != AudioFormat.NOT_SET) {
      ensureBuffer(outputAudioFormat.sampleRate, outputAudioFormat.channelCount)
    }
    if (!enabled) {
      clearBuffer()
    }
  }

  fun disable() {
    setEffect(enabled = false)
  }

  override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
    return when (inputAudioFormat.encoding) {
      C.ENCODING_PCM_16BIT, C.ENCODING_PCM_FLOAT -> {
        ensureBuffer(inputAudioFormat.sampleRate, inputAudioFormat.channelCount)
        inputAudioFormat
      }
      else -> throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
    }
  }

  override fun queueInput(inputBuffer: ByteBuffer) {
    if (!enabled || delayFrames <= 0 || delayBuffer.isEmpty()) {
      // Pass-through without touching the delay line.
      val output = replaceOutputBuffer(inputBuffer.remaining())
      output.put(inputBuffer)
      output.flip()
      return
    }

    val output = replaceOutputBuffer(inputBuffer.remaining())
    val channels = channelCount
    val frames = delayFrames
    val buf = delayBuffer
    var w = writeFrame
    val fb = feedback
    val wetMix = wet
    val dryMix = dry

    when (inputAudioFormat.encoding) {
      C.ENCODING_PCM_FLOAT -> {
        val bytesPerFrame = channels * 4
        while (inputBuffer.remaining() >= bytesPerFrame) {
          for (ch in 0 until channels) {
            val idx = w * channels + ch
            val input = inputBuffer.float
            val delayed = buf[idx]
            val out = dryMix * input + wetMix * delayed
            buf[idx] = input + fb * delayed
            output.putFloat(out.coerceIn(-1f, 1f))
          }
          w++
          if (w >= frames) w = 0
        }
      }
      C.ENCODING_PCM_16BIT -> {
        val bytesPerFrame = channels * 2
        while (inputBuffer.remaining() >= bytesPerFrame) {
          for (ch in 0 until channels) {
            val idx = w * channels + ch
            val input = inputBuffer.short / 32768f
            val delayed = buf[idx]
            val out = dryMix * input + wetMix * delayed
            buf[idx] = input + fb * delayed
            output.putShort((out.coerceIn(-1f, 1f) * 32767f).toInt().toShort())
          }
          w++
          if (w >= frames) w = 0
        }
      }
      else -> throw IllegalStateException("Unexpected encoding: ${inputAudioFormat.encoding}")
    }
    writeFrame = w
    output.flip()
  }

  override fun onFlush() {
    clearBuffer()
  }

  override fun onReset() {
    clearBuffer()
    delayBuffer = FloatArray(0)
    delayFrames = 0
    channelCount = 0
    writeFrame = 0
  }

  private fun ensureBuffer(sampleRate: Int, channels: Int) {
    if (sampleRate <= 0 || channels <= 0) return
    val frames = max(1, (delayMs / 1000f * sampleRate).roundToInt())
    if (frames == delayFrames && channels == channelCount && delayBuffer.size == frames * channels) {
      return
    }
    delayFrames = frames
    channelCount = channels
    delayBuffer = FloatArray(frames * channels)
    writeFrame = 0
  }

  private fun clearBuffer() {
    delayBuffer.fill(0f)
    writeFrame = 0
  }

  companion object {
    const val MIN_DELAY_MS = 40f
    const val MAX_DELAY_MS = 600f
    const val MAX_FEEDBACK = 0.85f
  }
}

/**
 * Maps UI echo preset indices to DSP delay parameters.
 * Index 0 = off / none (unused when enabled).
 */
object EchoPresets {
  data class Params(
    val delayMs: Float,
    val feedback: Float,
    val wet: Float,
    val dry: Float,
  )

  /** Presets aligned with [remix.myplayer.ui.dialog.EchoPresetNameRes] indices 1..6. */
  private val byIndex = arrayOf(
    Params(0f, 0f, 0f, 1f),           // 0 Off
    Params(120f, 0.28f, 0.38f, 0.9f), // 1 Small
    Params(200f, 0.35f, 0.42f, 0.88f), // 2 Medium
    Params(300f, 0.42f, 0.48f, 0.85f), // 3 Large
    Params(400f, 0.48f, 0.52f, 0.82f), // 4 Hall
    Params(520f, 0.55f, 0.58f, 0.8f),  // 5 Large hall
    Params(250f, 0.58f, 0.5f, 0.85f),  // 6 Plate
  )

  fun paramsFor(presetIndex: Int): Params {
    val index = presetIndex.coerceIn(0, byIndex.lastIndex)
    return byIndex[index]
  }
}
