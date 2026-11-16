package remix.myplayer.lyric

import android.content.Context
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import remix.myplayer.data.model.audio.Song
import remix.myplayer.data.prefs.LyricPrefs
import remix.myplayer.lyric.provider.ILyricsProvider
import remix.myplayer.service.MusicServiceRemote
import remix.myplayer.service.playback.MusicStateSource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricManager @Inject constructor(
  @param:ApplicationContext
  private val context: Context,
  val lyricPrefs: LyricPrefs,
  val lyricSearcher: LyricSearcher
) : CoroutineScope by CoroutineScope(Dispatchers.IO + SupervisorJob()) {

  private val _currentNextLyricsLine = MutableStateFlow(CurrentNextLyricsLine.SEARCHING)
  val currentNextLyricsLine = _currentNextLyricsLine.asStateFlow()

  @Volatile
  var isScreenOn: Boolean =
    (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
    set(value) {
      if (field != value) {
        field = value
        if (value) updateProgress()
      }
    }

  var isStatusBarLyricEnabled: Boolean
    get() = lyricPrefs.statusBarLyricEnabled
    set(value) {
      lyricPrefs.statusBarLyricEnabled = value
      updateStatusBarLyric()
    }

  var isTranslationEnabled: Boolean
    get() = lyricPrefs.translationEnabled
    set(value) {
      lyricPrefs.translationEnabled = value
    }

  var lyrics: List<LyricLine>? = null
    private set

  var isPlaying: Boolean = false
    set(value) {
      if (field == value) {
        return
      }
      field = value
      updateStatusBarLyric()
    }

  var offset: Long = 0
    set(value) {
      field = value
      launch(Dispatchers.IO) {
        updateProgress()
        lyricSearcher.saveOffset(MusicStateSource.currentPlaybackUiState.song, value)
      }
    }

  // For status bar lyrics
  private var currentLyricsLine: String = ""
    set(value) {
      if (value == field) {
        return
      }
      field = value
      updateStatusBarLyric()
    }

  private fun updateStatusBarLyric() {
    val service = MusicServiceRemote.service ?: return
    val lyricLine = currentLyricsLine
    val shouldShow = isStatusBarLyricEnabled && isPlaying &&
        lyricLine.isNotBlank() &&
        lyricLine != LyricLine.LYRICS_LINE_NO_LRC.content

    if (shouldShow) {
      service.updateNotificationWithLrc(lyricLine)
    } else {
      service.clearStatusBarLyricNotification()
    }
  }

  private val updateMutex = Mutex()
  private var updateLyricsJob: Job? = null

  init {
    launch(Dispatchers.Main) {
      MusicStateSource.progressState.collect {
        updateProgress()
      }
    }
    launch(Dispatchers.Main) {
      currentNextLyricsLine.collect { line ->
        currentLyricsLine = line.currentLine?.content ?: ""
      }
    }
    launch(Dispatchers.Main) {
      var lastSong: Song? = null
      MusicStateSource.playbackUiState.collect { state ->
        if (isPlaying != state.isPlaying) {
          isPlaying = state.isPlaying
        }
        if (state.song != lastSong) {
          lastSong = state.song
          updateLyrics(state.song, null)
        }
      }
    }
  }

  private fun getProgressOfLine(line: LyricLine, time: Long, endTime: Long): Double {
    if (endTime <= line.time) {
      Timber.tag(TAG).w("Invalid line range, time=$time, lineTime=${line.time}, endTime=$endTime")
      return computeLineProgress(line, time, endTime)
    }

    val clampedTime = time.coerceIn(line.time, endTime)
    if (clampedTime != time) {
      Timber.tag(TAG).w("Clamped time, time=$time, lineTime=${line.time}, endTime=$endTime")
    }

    return computeLineProgress(line, time, endTime)
  }

  private fun getCurrentNextLine(
    lyrics: List<LyricLine>, offset: Long, progress: Long, duration: Long
  ): CurrentNextLyricsLine {
    if (lyrics.isEmpty()) {
      return CurrentNextLyricsLine(LyricLine.LYRICS_LINE_NO_LRC, null, null)
    }
    val progressWithOffset = progress + offset
    val index = lyrics.binarySearchBy(progressWithOffset) { it.time }.let {
      if (it < 0) -(it + 1) - 1 else it
    }
    if (index < 0) {
      check(index == -1)
      return CurrentNextLyricsLine(null, null, lyrics[0])
    }
    check(index < lyrics.size)
    val cur = lyrics[index]
    val nxt = lyrics.getOrNull(index + 1)
    return CurrentNextLyricsLine(
      cur, getProgressOfLine(cur, progressWithOffset, nxt?.time ?: (duration + offset)), nxt
    )
  }

  private fun updateProgress() {
    if (!isScreenOn) return
    if (!updateMutex.tryLock()) {
      // 还没拿到歌词或者当前有别的线程在更新
      return
    }
    try {
      val currentLyrics = lyrics ?: return
      val state = MusicStateSource.currentProgressState
      _currentNextLyricsLine.value =
        getCurrentNextLine(currentLyrics, offset, state.position, state.duration)
    } finally {
      updateMutex.unlock()
    }
  }

  fun updateLyrics(song: Song, provider: ILyricsProvider? = null): Job? {
    if (!song.valid()) {
      return null
    }
    updateLyricsJob?.cancel()
    updateLyricsJob = launch(Dispatchers.IO) {
      updateMutex.withLock {
        lyrics = null
        _currentNextLyricsLine.value = CurrentNextLyricsLine.SEARCHING
        val s = lyricSearcher.getLyricsAndOffset(song, provider)
        ensureActive()
        lyrics = LrcParser.mergeTranslations(s.first, lyricPrefs.translationEnabled)
        offset = s.second
      }
      updateProgress()
    }
    return updateLyricsJob
  }

  fun clearCache(song: Song) {
    launch(Dispatchers.IO) {
      lyricSearcher.clearCache(song)
    }
  }

  fun clearAllCache(includePersistent: Boolean = false) {
    launch(Dispatchers.IO) {
      lyricSearcher.clearAllCache(includePersistent)
    }
  }

  companion object {

    private const val TAG = "LyricsManager"

    /**
     * 计算当前行的播放进度：普通歌词返回 0~1，逐字歌词返回 0~words.size
     *
     * @param line 当前歌词行
     * @param time 当前播放时间
     * @param endTime 当前行的结束时间
     */
    fun computeLineProgress(line: LyricLine, time: Long, endTime: Long): Double {
      if (endTime <= line.time) {
        return when (line) {
          is PerWordLyricLine -> if (time > line.time) line.words.size.toDouble() else 0.0
          else -> if (time > line.time) 1.0 else 0.0
        }
      }

      val clampedTime = time.coerceIn(line.time, endTime)
      return if (line is PerWordLyricLine) {
        line.getProgress(clampedTime, endTime)
      } else {
        (clampedTime - line.time).toDouble() / (endTime - line.time)
      }
    }

    const val ACTION_LYRIC = "action_lyric"

    const val EXTRA_LYRIC = "extra_lyric"
    const val EXTRA_LYRIC_URI = "extra_lyric_uri"

    const val CHANGE_LYRIC = 1
    const val CHANGE_LYRIC_FONT_SCALE = 2
    const val SHOW_OFFSET_PANEL = 3
  }
}
