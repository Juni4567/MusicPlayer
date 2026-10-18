package remix.myplayer.helper.trim

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Client-side audio trimmer using FFmpegKit.
 * Prefers lossless stream copy; falls back to a light re-encode when copy fails.
 */
object AudioTrimEngine {

  const val MIN_CLIP_MS = 500L

  suspend fun trim(
    context: Context,
    inputUri: Uri,
    inputPath: String?,
    startMs: Long,
    endMs: Long,
    durationMs: Long,
    outputFile: File,
  ): AudioTrimResult = withContext(Dispatchers.IO) {
    if (startMs < 0 || endMs > durationMs || startMs >= endMs || endMs - startMs < MIN_CLIP_MS) {
      return@withContext AudioTrimResult.Error.InvalidRange
    }

    val input = resolveInputPath(context, inputUri, inputPath)
      ?: return@withContext AudioTrimResult.Error.Io("Unable to open input")

    if (outputFile.exists()) {
      outputFile.delete()
    }
    outputFile.parentFile?.mkdirs()

    val startSec = msToSeconds(startMs)
    val endSec = msToSeconds(endMs)
    val quotedInput = quote(input)
    val quotedOutput = quote(outputFile.absolutePath)

    // Primary: stream copy (no quality loss).
    val copyCmd =
      "-y -ss $startSec -to $endSec -i $quotedInput -c copy $quotedOutput"
    val copyResult = runFfmpeg(copyCmd)
    if (copyResult is AudioTrimResult.Success && outputFile.exists() && outputFile.length() > 0) {
      return@withContext copyResult
    }
    Timber.w("Stream copy trim failed, falling back to re-encode: $copyResult")
    if (outputFile.exists()) {
      outputFile.delete()
    }

    val encodeArgs = reEncodeArgs(outputFile.name)
      ?: return@withContext AudioTrimResult.Error.Unsupported
    val encodeCmd =
      "-y -ss $startSec -to $endSec -i $quotedInput $encodeArgs $quotedOutput"
    val encodeResult = runFfmpeg(encodeCmd)
    if (encodeResult is AudioTrimResult.Success && outputFile.exists() && outputFile.length() > 0) {
      return@withContext encodeResult
    }
    if (outputFile.exists()) {
      outputFile.delete()
    }
    encodeResult
  }

  private fun runFfmpeg(command: String): AudioTrimResult {
    Timber.v("FFmpeg trim: $command")
    return try {
      val session = FFmpegKit.execute(command)
      if (ReturnCode.isSuccess(session.returnCode)) {
        AudioTrimResult.Success(session.output ?: "")
      } else {
        val msg = session.failStackTrace?.takeIf { it.isNotBlank() }
          ?: session.allLogsAsString?.takeLast(400)
          ?: "FFmpeg failed (${session.returnCode})"
        Timber.e("FFmpeg trim failed: $msg")
        AudioTrimResult.Error.FFmpeg(msg)
      }
    } catch (e: Exception) {
      Timber.e(e, "FFmpeg trim exception")
      AudioTrimResult.Error.FFmpeg(e.message ?: e.toString())
    }
  }

  private fun reEncodeArgs(fileName: String): String? {
    val ext = fileName.substringAfterLast('.', "").lowercase(Locale.US)
    return when (ext) {
      "mp3" -> "-c:a libmp3lame -q:a 2"
      "m4a", "aac", "mp4" -> "-c:a aac -b:a 192k"
      "ogg", "oga" -> "-c:a libvorbis -q:a 5"
      "opus" -> "-c:a libopus -b:a 128k"
      "wav" -> "-c:a pcm_s16le"
      "flac" -> "-c:a flac"
      else -> null
    }
  }

  private fun resolveInputPath(context: Context, uri: Uri, path: String?): String? {
    if (!path.isNullOrBlank()) {
      val file = File(path)
      if (file.exists() && file.canRead()) {
        return file.absolutePath
      }
    }
    // Copy content URI to a cache file FFmpeg can open by path.
    return try {
      val ext = path?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }
        ?: uri.lastPathSegment?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }
        ?: "mp3"
      val temp = File(context.cacheDir, "trim_input_${System.currentTimeMillis()}.$ext")
      context.contentResolver.openInputStream(uri)?.use { input ->
        temp.outputStream().use { output -> input.copyTo(output) }
      } ?: return null
      temp.absolutePath
    } catch (e: Exception) {
      Timber.e(e, "Failed to materialize trim input")
      null
    }
  }

  private fun msToSeconds(ms: Long): String {
    val seconds = ms / 1000.0
    return String.format(Locale.US, "%.3f", seconds)
  }

  private fun quote(path: String): String {
    // Escape single quotes for shell-style ffmpeg-kit argument parsing.
    return "'${path.replace("'", "'\\''")}'"
  }

  fun createTempOutput(context: Context, sourcePath: String): File {
    val ext = sourcePath.substringAfterLast('.', "mp3").ifBlank { "mp3" }
    return File(context.cacheDir, "trim_out_${System.currentTimeMillis()}.$ext")
  }

  fun formatRange(startMs: Long, endMs: Long): String {
    fun fmt(ms: Long): String {
      val totalSec = TimeUnit.MILLISECONDS.toSeconds(ms.coerceAtLeast(0))
      val m = totalSec / 60
      val s = totalSec % 60
      return String.format(Locale.US, "%d:%02d", m, s)
    }
    return "${fmt(startMs)} – ${fmt(endMs)}"
  }
}
