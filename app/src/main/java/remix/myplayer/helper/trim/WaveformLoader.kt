package remix.myplayer.helper.trim

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Decodes audio off the main thread and samples amplitude peaks for the trim UI.
 */
object WaveformLoader {

  private const val TARGET_BARS = 300
  private const val TIMEOUT_US = 10_000L

  private val cache = LruCache<Long, FloatArray>(24)

  suspend fun load(
    context: Context,
    songId: Long,
    uri: Uri,
    path: String?,
  ): FloatArray = withContext(Dispatchers.IO) {
    cache.get(songId)?.let { return@withContext it }
    val peaks = runCatching { decodePeaks(context, uri, path) }
      .onFailure { Timber.w(it, "Waveform decode failed") }
      .getOrElse { FloatArray(TARGET_BARS) { 0.15f } }
    cache.put(songId, peaks)
    peaks
  }

  fun invalidate(songId: Long) {
    cache.remove(songId)
  }

  private fun decodePeaks(context: Context, uri: Uri, path: String?): FloatArray {
    val extractor = MediaExtractor()
    try {
      if (!path.isNullOrBlank() && java.io.File(path).exists()) {
        extractor.setDataSource(path)
      } else {
        extractor.setDataSource(context, uri, null)
      }

      val trackIndex = (0 until extractor.trackCount).firstOrNull { i ->
        extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
      } ?: return FloatArray(TARGET_BARS) { 0.15f }

      extractor.selectTrack(trackIndex)
      val format = extractor.getTrackFormat(trackIndex)
      val mime = format.getString(MediaFormat.KEY_MIME) ?: return FloatArray(TARGET_BARS) { 0.15f }
      val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) {
        format.getLong(MediaFormat.KEY_DURATION)
      } else {
        0L
      }
      if (durationUs <= 0L) {
        return FloatArray(TARGET_BARS) { 0.15f }
      }

      val codec = MediaCodec.createDecoderByType(mime)
      codec.configure(format, null, null, 0)
      codec.start()

      val windowUs = max(1L, durationUs / TARGET_BARS)
      val peaks = FloatArray(TARGET_BARS)
      val windowMax = FloatArray(TARGET_BARS)
      var inputDone = false
      var outputDone = false
      val info = MediaCodec.BufferInfo()

      while (!outputDone) {
        if (!inputDone) {
          val inIndex = codec.dequeueInputBuffer(TIMEOUT_US)
          if (inIndex >= 0) {
            val buffer = codec.getInputBuffer(inIndex)!!
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) {
              codec.queueInputBuffer(
                inIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM
              )
              inputDone = true
            } else {
              codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
              extractor.advance()
            }
          }
        }

        val outIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US)
        when {
          outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
          outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Unit
          outIndex >= 0 -> {
            if (info.size > 0 && info.presentationTimeUs >= 0) {
              val outBuffer = codec.getOutputBuffer(outIndex)!!
              outBuffer.position(info.offset)
              outBuffer.limit(info.offset + info.size)
              val bar = min(
                TARGET_BARS - 1,
                (info.presentationTimeUs / windowUs).toInt()
              )
              // 16-bit PCM assumed for most decoders; sample absolute peaks.
              var peak = windowMax[bar]
              val remaining = info.size - (info.size % 2)
              var i = 0
              while (i < remaining) {
                val sample = outBuffer.getShort(info.offset + i).toInt()
                peak = max(peak, abs(sample).toFloat())
                i += 2
              }
              windowMax[bar] = peak
            }
            codec.releaseOutputBuffer(outIndex, false)
            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
              outputDone = true
            }
          }
        }
      }

      codec.stop()
      codec.release()

      var globalMax = 1f
      for (v in windowMax) {
        if (v > globalMax) globalMax = v
      }
      for (i in peaks.indices) {
        peaks[i] = (windowMax[i] / globalMax).coerceIn(0.05f, 1f)
      }
      // Fill empty gaps with neighbors so the canvas isn't sparse.
      for (i in peaks.indices) {
        if (windowMax[i] <= 0f) {
          val prev = peaks.getOrElse(i - 1) { 0.12f }
          val next = peaks.getOrElse(i + 1) { prev }
          peaks[i] = ((prev + next) / 2f).coerceIn(0.05f, 1f)
        }
      }
      return peaks
    } finally {
      extractor.release()
    }
  }
}
