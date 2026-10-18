package remix.myplayer.helper.trim

import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.service.MusicService
import remix.myplayer.ui.activity.base.BaseActivity
import remix.myplayer.ui.nav.MessageNotifier
import remix.myplayer.util.Util
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

object AudioTrimWriter {

  data class PendingTrimOverwrite(
    val song: Song,
    val trimmedFile: File,
    val newDurationMs: Long,
  )

  /**
   * Request write access (R+) then overwrite the original song file with [trimmedFile].
   */
  fun requestOverwrite(
    activity: BaseActivity,
    song: Song,
    trimmedFile: File,
    newDurationMs: Long,
  ) {
    val request = PendingTrimOverwrite(song, trimmedFile, newDurationMs)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      activity.pendingTrimOverwrite = request
      activity.trimWriteLauncher.launch(
        IntentSenderRequest.Builder(
          MediaStore.createWriteRequest(
            activity.contentResolver,
            listOf(song.contentUri)
          ).intentSender
        ).build()
      )
    } else {
      activity.lifecycleScope.launch {
        try {
          completeOverwrite(activity, request, withFallback = true)
        } catch (e: Exception) {
          try {
            activity.contentResolver.openFileDescriptor(song.contentUri, "w")?.close()
          } catch (securityException: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
              securityException is RecoverableSecurityException
            ) {
              activity.pendingTrimOverwrite = request
              activity.trimWriteLauncher.launch(
                IntentSenderRequest.Builder(
                  securityException.userAction.actionIntent.intentSender
                ).build()
              )
              return@launch
            }
          }
          Timber.e(e, "Fail to overwrite trimmed audio")
          MessageNotifier.show(R.string.trim_error_permission)
          trimmedFile.delete()
        }
      }
    }
  }

  suspend fun completeOverwrite(
    context: Context,
    request: PendingTrimOverwrite,
    withFallback: Boolean = true,
  ) = withContext(Dispatchers.IO) {
    try {
      overwriteFile(context, request, withFallback)
      scanAndNotify(context, request.song.data, request.song.contentUri)
      WaveformLoader.invalidate(request.song.id)
      withContext(Dispatchers.Main) {
        MessageNotifier.show(R.string.trim_success)
      }
    } finally {
      request.trimmedFile.delete()
    }
  }

  /**
   * Insert a new MediaStore audio row next to the original and write [trimmedFile] into it.
   */
  suspend fun saveAsNew(
    context: Context,
    song: Song,
    trimmedFile: File,
    newDurationMs: Long,
  ): Uri = withContext(Dispatchers.IO) {
    try {
      val uri = insertAndWrite(context, song, trimmedFile, newDurationMs)
      val path = resolvePath(context, uri) ?: song.data
      scanAndNotify(context, path, uri)
      withContext(Dispatchers.Main) {
        MessageNotifier.show(R.string.trim_success)
      }
      uri
    } finally {
      trimmedFile.delete()
    }
  }

  private fun overwriteFile(
    context: Context,
    request: PendingTrimOverwrite,
    withFallback: Boolean,
  ) {
    val song = request.song
    val trimmed = request.trimmedFile
    val direct = runCatching {
      val dest = File(song.data)
      if (!dest.exists() || !dest.canWrite()) {
        throw IOException("Cannot write ${song.data}")
      }
      FileInputStream(trimmed).use { input ->
        FileOutputStream(dest).use { output ->
          output.channel.truncate(0)
          input.copyTo(output)
        }
      }
    }
    if (direct.isSuccess) {
      return
    }
    if (!withFallback || !song.isLocal() || song.id <= 0) {
      throw direct.exceptionOrNull()!!
    }
    Timber.v("Fallback to content uri overwrite: ${song.data}")
    context.contentResolver.openFileDescriptor(song.contentUri, "rw")?.use { pfd ->
      FileInputStream(trimmed).use { input ->
        FileOutputStream(pfd.fileDescriptor).use { output ->
          output.channel.truncate(0)
          input.copyTo(output)
        }
      }
    } ?: throw IOException("Unable to open file descriptor for ${song.contentUri}")
  }

  private fun insertAndWrite(
    context: Context,
    song: Song,
    trimmedFile: File,
    newDurationMs: Long,
  ): Uri {
    val ext = song.data.substringAfterLast('.', "mp3").ifBlank { "mp3" }
    val baseName = song.displayName
      .substringBeforeLast('.')
      .ifBlank { song.title.ifBlank { "trimmed" } }
    val displayName = uniqueDisplayName(context, song, baseName, ext)
    val mime = mimeForExt(ext)
    val resolver = context.contentResolver

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val relativePath = relativePathFor(song)
      val values = ContentValues().apply {
        put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Audio.Media.MIME_TYPE, mime)
        put(MediaStore.Audio.Media.RELATIVE_PATH, relativePath)
        put(MediaStore.Audio.Media.IS_PENDING, 1)
        put(MediaStore.Audio.Media.DURATION, newDurationMs)
        put(MediaStore.Audio.Media.TITLE, "$baseName trim")
        if (song.album.isNotBlank()) put(MediaStore.Audio.Media.ALBUM, song.album)
        if (song.artist.isNotBlank()) put(MediaStore.Audio.Media.ARTIST, song.artist)
      }
      val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        ?: throw IOException("MediaStore insert failed")
      try {
        resolver.openOutputStream(uri)?.use { output ->
          FileInputStream(trimmedFile).use { input -> input.copyTo(output) }
        } ?: throw IOException("Unable to open output stream for $uri")
        values.clear()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
      } catch (e: Exception) {
        resolver.delete(uri, null, null)
        throw e
      }
    }

    // Pre-Q: write beside the original file and scan.
    val parent = File(song.data).parentFile
      ?: File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath)
    parent.mkdirs()
    var dest = File(parent, displayName)
    var n = 1
    while (dest.exists()) {
      dest = File(parent, "${baseName}_trim_$n.$ext")
      n++
    }
    FileInputStream(trimmedFile).use { input ->
      FileOutputStream(dest).use { output -> input.copyTo(output) }
    }
    return Uri.fromFile(dest)
  }

  private fun uniqueDisplayName(
    context: Context,
    song: Song,
    baseName: String,
    ext: String,
  ): String {
    val candidate = "${baseName}_trim.$ext"
    val parent = File(song.data).parentFile
    if (parent != null) {
      var name = candidate
      var n = 1
      while (File(parent, name).exists()) {
        name = "${baseName}_trim_$n.$ext"
        n++
      }
      return name
    }
    // MediaStore uniqueness is handled by the provider; still prefer a stable name.
    return candidate
  }

  private fun relativePathFor(song: Song): String {
    val data = song.data
    val musicRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
      .absolutePath
    val parent = File(data).parent ?: return Environment.DIRECTORY_MUSIC
    return if (parent.startsWith(musicRoot)) {
      val rel = parent.removePrefix(
        Environment.getExternalStorageDirectory().absolutePath
      ).trimStart('/')
      if (rel.isBlank()) Environment.DIRECTORY_MUSIC else rel
    } else {
      // Keep under Music/<folder> when original is outside Music.
      val folder = File(parent).name
      "${Environment.DIRECTORY_MUSIC}/$folder"
    }
  }

  private fun mimeForExt(ext: String): String {
    return when (ext.lowercase(Locale.US)) {
      "mp3" -> "audio/mpeg"
      "m4a", "mp4", "aac" -> "audio/mp4"
      "flac" -> "audio/flac"
      "ogg", "oga" -> "audio/ogg"
      "opus" -> "audio/opus"
      "wav" -> "audio/wav"
      "wma" -> "audio/x-ms-wma"
      else -> "audio/*"
    }
  }

  private fun resolvePath(context: Context, uri: Uri): String? {
    if (uri.scheme == "file") {
      return uri.path
    }
    return try {
      context.contentResolver.query(
        uri,
        arrayOf(MediaStore.Audio.Media.DATA),
        null,
        null,
        null
      )?.use { cursor ->
        if (cursor.moveToFirst()) {
          cursor.getString(0)
        } else {
          null
        }
      }
    } catch (e: Exception) {
      Timber.w(e, "resolvePath failed for $uri")
      null
    }
  }

  private suspend fun scanAndNotify(context: Context, path: String, fallbackUri: Uri) {
    val scanned = suspendCancellableCoroutine { cont ->
      MediaScannerConnection.scanFile(context, arrayOf(path), null) { _, uri ->
        if (cont.isActive) {
          cont.resume(uri ?: fallbackUri)
        }
      }
    }
    context.contentResolver.notifyChange(scanned, null)
    Util.sendLocalBroadcast(Intent(MusicService.MEDIA_STORE_CHANGE))
  }
}
