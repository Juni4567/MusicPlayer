package remix.myplayer.viewmodel

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.helper.trim.AudioTrimEngine
import remix.myplayer.helper.trim.AudioTrimResult
import remix.myplayer.helper.trim.AudioTrimWriter
import remix.myplayer.helper.trim.WaveformLoader
import remix.myplayer.ui.activity.base.BaseActivity
import remix.myplayer.ui.nav.MessageNotifier
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class AudioTrimViewModel @Inject constructor(
  application: Application,
) : AndroidViewModel(application) {

  private val _state = MutableStateFlow(AudioTrimState())
  val state = _state.asStateFlow()

  private var previewPlayer: ExoPlayer? = null
  private var progressJob: Job? = null

  fun startTrim(song: Song) {
    releasePreview()
    val duration = song.duration.coerceAtLeast(AudioTrimEngine.MIN_CLIP_MS)
    _state.value = AudioTrimState(
      song = song,
      durationMs = duration,
      startMs = 0L,
      endMs = duration,
      replaceOriginal = false,
      isLoadingWaveform = true,
      waveform = FloatArray(0),
    )
    viewModelScope.launch {
      val peaks = WaveformLoader.load(
        getApplication(),
        song.id,
        song.contentUri,
        song.data,
      )
      _state.update { current ->
        if (current.song?.id != song.id) current
        else current.copy(waveform = peaks, isLoadingWaveform = false)
      }
    }
  }

  fun setStartMs(ms: Long) {
    _state.update { s ->
      val maxStart = s.endMs - AudioTrimEngine.MIN_CLIP_MS
      s.copy(startMs = ms.coerceIn(0L, max(0L, maxStart)))
    }
  }

  fun setEndMs(ms: Long) {
    _state.update { s ->
      val minEnd = s.startMs + AudioTrimEngine.MIN_CLIP_MS
      s.copy(endMs = ms.coerceIn(minEnd, s.durationMs))
    }
  }

  fun setReplaceOriginal(replace: Boolean) {
    _state.update { it.copy(replaceOriginal = replace) }
  }

  fun togglePreview() {
    val s = _state.value
    val song = s.song ?: return
    if (s.isPreviewing) {
      stopPreview()
      return
    }
    val player = previewPlayer ?: ExoPlayer.Builder(getApplication()).build().also { p ->
      previewPlayer = p
      p.addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
          if (playbackState == Player.STATE_ENDED) {
            stopPreview()
          }
        }
      })
    }
    val item = MediaItem.Builder()
      .setUri(song.contentUri)
      .setClippingConfiguration(
        MediaItem.ClippingConfiguration.Builder()
          .setStartPositionMs(s.startMs)
          .setEndPositionMs(s.endMs)
          .build()
      )
      .build()
    player.setMediaItem(item)
    player.prepare()
    player.playWhenReady = true
    _state.update { it.copy(isPreviewing = true, previewPositionMs = s.startMs) }
    progressJob?.cancel()
    progressJob = viewModelScope.launch {
      while (isActive) {
        val pos = previewPlayer?.currentPosition ?: break
        _state.update { cur ->
          cur.copy(previewPositionMs = cur.startMs + pos.coerceAtLeast(0L))
        }
        delay(50)
      }
    }
  }

  fun stopPreview() {
    progressJob?.cancel()
    progressJob = null
    previewPlayer?.pause()
    previewPlayer?.stop()
    _state.update { it.copy(isPreviewing = false) }
  }

  fun save(activity: BaseActivity) {
    val s = _state.value
    val song = s.song ?: return
    if (s.isSaving) return
    stopPreview()

    if (!song.isLocal()) {
      MessageNotifier.show(R.string.trim_local_only)
      return
    }
    if (s.endMs - s.startMs < AudioTrimEngine.MIN_CLIP_MS) {
      MessageNotifier.show(R.string.trim_error_range)
      return
    }

    _state.update { it.copy(isSaving = true, errorMessage = null) }
    viewModelScope.launch {
      val context = getApplication<Application>()
      val output = AudioTrimEngine.createTempOutput(context, song.data)
      val result = AudioTrimEngine.trim(
        context = context,
        inputUri = song.contentUri,
        inputPath = song.data,
        startMs = s.startMs,
        endMs = s.endMs,
        durationMs = s.durationMs,
        outputFile = output,
      )
      when (result) {
        is AudioTrimResult.Success -> {
          val newDuration = s.endMs - s.startMs
          try {
            if (s.replaceOriginal) {
              // Permission dialog may follow; exit like tag edit and finish write in BaseActivity.
              AudioTrimWriter.requestOverwrite(activity, song, output, newDuration)
            } else {
              withContext(Dispatchers.IO) {
                AudioTrimWriter.saveAsNew(context, song, output, newDuration)
              }
            }
            _state.update { it.copy(isSaving = false, saveCompleted = true) }
          } catch (e: Exception) {
            Timber.e(e, "Trim save failed")
            output.delete()
            _state.update {
              it.copy(
                isSaving = false,
                errorMessage = e.message ?: e.toString()
              )
            }
            MessageNotifier.show(R.string.trim_error_generic, e.message ?: e.toString())
          }
        }
        is AudioTrimResult.Error.InvalidRange -> {
          output.delete()
          _state.update { it.copy(isSaving = false) }
          MessageNotifier.show(R.string.trim_error_range)
        }
        is AudioTrimResult.Error.Unsupported -> {
          output.delete()
          _state.update { it.copy(isSaving = false) }
          MessageNotifier.show(R.string.trim_error_unsupported)
        }
        is AudioTrimResult.Error.Permission -> {
          output.delete()
          _state.update { it.copy(isSaving = false) }
          MessageNotifier.show(R.string.trim_error_permission)
        }
        is AudioTrimResult.Error.FFmpeg -> {
          output.delete()
          _state.update { it.copy(isSaving = false) }
          MessageNotifier.show(R.string.trim_error_generic, result.message)
        }
        is AudioTrimResult.Error.Io -> {
          output.delete()
          _state.update { it.copy(isSaving = false) }
          MessageNotifier.show(R.string.trim_error_generic, result.message)
        }
      }
    }
  }

  private fun releasePreview() {
    progressJob?.cancel()
    progressJob = null
    previewPlayer?.release()
    previewPlayer = null
  }

  override fun onCleared() {
    releasePreview()
    super.onCleared()
  }
}

@Stable
data class AudioTrimState(
  val song: Song? = null,
  val durationMs: Long = 0L,
  val startMs: Long = 0L,
  val endMs: Long = 0L,
  val waveform: FloatArray = FloatArray(0),
  val isLoadingWaveform: Boolean = false,
  val replaceOriginal: Boolean = false,
  val isSaving: Boolean = false,
  val isPreviewing: Boolean = false,
  val previewPositionMs: Long = 0L,
  val errorMessage: String? = null,
  val saveCompleted: Boolean = false,
) {
  val selectionLabel: String
    get() = AudioTrimEngine.formatRange(startMs, endMs)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AudioTrimState) return false
    return song?.id == other.song?.id &&
      durationMs == other.durationMs &&
      startMs == other.startMs &&
      endMs == other.endMs &&
      isLoadingWaveform == other.isLoadingWaveform &&
      replaceOriginal == other.replaceOriginal &&
      isSaving == other.isSaving &&
      isPreviewing == other.isPreviewing &&
      previewPositionMs == other.previewPositionMs &&
      errorMessage == other.errorMessage &&
      saveCompleted == other.saveCompleted &&
      waveform.contentEquals(other.waveform)
  }

  override fun hashCode(): Int {
    var result = song?.id?.hashCode() ?: 0
    result = 31 * result + durationMs.hashCode()
    result = 31 * result + startMs.hashCode()
    result = 31 * result + endMs.hashCode()
    result = 31 * result + waveform.contentHashCode()
    result = 31 * result + isLoadingWaveform.hashCode()
    result = 31 * result + replaceOriginal.hashCode()
    result = 31 * result + isSaving.hashCode()
    result = 31 * result + isPreviewing.hashCode()
    result = 31 * result + previewPositionMs.hashCode()
    result = 31 * result + (errorMessage?.hashCode() ?: 0)
    result = 31 * result + saveCompleted.hashCode()
    return result
  }
}
