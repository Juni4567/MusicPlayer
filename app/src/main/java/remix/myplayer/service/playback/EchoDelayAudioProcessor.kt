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
 * Feedback delay (echo) in the PCM domain.
 *
 * Always fully consumes [queueInput] buffers (Media3 requirement). Effect
 * parameters may be updated from the main thread; processing snapshots them
 * under a lock so the audio thread never sees a torn delay line.
 */
@OptIn(UnstableApi::class)
class EchoDelayAudioProcessor : BaseAudioProcessor() {

  private val lock = Any()

  @Volatile
  private var enabled = false

  private var delayMs = 200f
  private var feedback = 0.35f
  private var wet = 0.4f
  private var dry = 0.85f

  private var delayBuffer: FloatArray = FloatArray(0)
  private var writeFrame = 0
  private var delayFrames = 0
  private var channelCount = 0
  private var sampleRate = 0

  fun setEffect(
    enabled: Boolean,
    delayMs: Float = this.delayMs,
    feedback: Float = this.feedback,
    wet: Float = this.wet,
    dry: Float = this.dry,
  ) {
    synchronized(lock) {
      this.enabled = enabled
      this.delayMs = delayMs.coerceIn(MIN_DELAY_MS, MAX_DELAY_MS)
      this.feedback = feedback.coerceIn(0f, MAX_FEEDBACK)
      this.wet = wet.coerceIn(0f, 1f)
      this.dry = dry.coerceIn(0f, 1f)
      if (sampleRate > 0 && channelCount > 0) {
        rebuildBufferLocked(sampleRate, channelCount)
      }
      if (!enabled) {
        delayBuffer.fill(0f)
        writeFrame = 0
      }
    }
  }

  fun disable() {
    setEffect(enabled = false)
  }

  override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
    return when (inputAudioFormat.encoding) {
      C.ENCODING_PCM_16BIT, C.ENCODING_PCM_FLOAT -> {
        synchronized(lock) {
          sampleRate = inputAudioFormat.sampleRate
          channelCount = inputAudioFormat.channelCount
          rebuildBufferLocked(sampleRate, channelCount)
        }
        inputAudioFormat
      }
      else -> throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
    }
  }

  override fun queueInput(inputBuffer: ByteBuffer) {
    val remaining = inputBuffer.remaining()
    if (remaining == 0) {
      replaceOutputBuffer(0).flip()
      return
    }
    val output = replaceOutputBuffer(remaining)

    val active: Boolean
    val channels: Int
    val frames: Int
    val buf: FloatArray
    var w: Int
    val fb: Float
    val wetMix: Float
    val dryMix: Float

    synchronized(lock) {
      active = enabled && delayFrames > 0 && delayBuffer.isNotEmpty() && channelCount > 0
      channels = channelCount
      frames = delayFrames
      buf = delayBuffer
      w = writeFrame
      fb = feedback
      wetMix = wet
      dryMix = dry
    }

    if (!active || channels <= 0) {
      // Full passthrough — always empty the input buffer.
      output.put(inputBuffer)
      output.flip()
      return
    }

    when (inputAudioFormat.encoding) {
      C.ENCODING_PCM_FLOAT -> {
        val bytesPerFrame = channels * 4
        while (inputBuffer.remaining() >= bytesPerFrame) {
          for (ch in 0 until channels) {
            val idx = w * channels + ch
            val input = inputBuffer.float
            val delayed = buf[idx]
            output.putFloat((dryMix * input + wetMix * delayed).coerceIn(-1f, 1f))
            buf[idx] = input + fb * delayed
          }
          w++
          if (w >= frames) w = 0
        }
        // Consume any trailing incomplete frame as dry passthrough.
        while (inputBuffer.hasRemaining()) {
          output.putFloat(inputBuffer.float)
        }
      }
      C.ENCODING_PCM_16BIT -> {
        val bytesPerFrame = channels * 2
        while (inputBuffer.remaining() >= bytesPerFrame) {
          for (ch in 0 until channels) {
            val idx = w * channels + ch
            val input = inputBuffer.short / 32768f
            val delayed = buf[idx]
            val out = (dryMix * input + wetMix * delayed).coerceIn(-1f, 1f)
            output.putShort((out * 32767f).toInt().toShort())
            buf[idx] = input + fb * delayed
          }
          w++
          if (w >= frames) w = 0
        }
        while (inputBuffer.hasRemaining()) {
          output.putShort(inputBuffer.short)
        }
      }
      else -> {
        output.put(inputBuffer)
      }
    }

    synchronized(lock) {
      // Only commit write cursor if the buffer instance is unchanged.
      if (buf === delayBuffer && frames == delayFrames && channels == channelCount) {
        writeFrame = w
      }
    }
    output.flip()
  }

  override fun onFlush() {
    synchronized(lock) {
      delayBuffer.fill(0f)
      writeFrame = 0
    }
  }

  override fun onReset() {
    synchronized(lock) {
      delayBuffer = FloatArray(0)
      delayFrames = 0
      channelCount = 0
      sampleRate = 0
      writeFrame = 0
      enabled = false
    }
  }

  private fun rebuildBufferLocked(rate: Int, channels: Int) {
    if (rate <= 0 || channels <= 0) return
    val frames = max(1, (delayMs / 1000f * rate).roundToInt())
    if (frames == delayFrames && channels == channelCount && delayBuffer.size == frames * channels) {
      return
    }
    delayFrames = frames
    channelCount = channels
    sampleRate = rate
    delayBuffer = FloatArray(frames * channels)
    writeFrame = 0
  }

  companion object {
    const val MIN_DELAY_MS = 40f
    const val MAX_DELAY_MS = 600f
    const val MAX_FEEDBACK = 0.85f
  }
}

/** Maps UI echo preset indices to DSP delay parameters. */
object EchoPresets {
  data class Params(
    val delayMs: Float,
    val feedback: Float,
    val wet: Float,
    val dry: Float,
  )

  private val byIndex = arrayOf(
    Params(0f, 0f, 0f, 1f),            // 0 Off
    Params(120f, 0.28f, 0.38f, 0.9f),  // 1 Small
    Params(200f, 0.35f, 0.42f, 0.88f), // 2 Medium
    Params(300f, 0.42f, 0.48f, 0.85f), // 3 Large
    Params(400f, 0.48f, 0.52f, 0.82f), // 4 Hall
    Params(520f, 0.55f, 0.58f, 0.8f),  // 5 Large hall
    Params(250f, 0.58f, 0.5f, 0.85f),  // 6 Plate
  )

  fun paramsFor(presetIndex: Int): Params =
    byIndex[presetIndex.coerceIn(0, byIndex.lastIndex)]
}
