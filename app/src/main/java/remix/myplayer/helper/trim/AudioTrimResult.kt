package remix.myplayer.helper.trim

sealed class AudioTrimResult {
  data class Success(val outputPath: String) : AudioTrimResult()

  sealed class Error : AudioTrimResult() {
    data object Permission : Error()
    data object Unsupported : Error()
    data object InvalidRange : Error()
    data class FFmpeg(val message: String) : Error()
    data class Io(val message: String) : Error()
  }
}
