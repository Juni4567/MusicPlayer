package remix.myplayer.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages transient in-app Volume Boost HUD display when volume exceeds 100%.
 */
object VolumeBoostHudController {
  private val _boostState = MutableStateFlow<Float?>(null)
  val boostState = _boostState.asStateFlow()

  private var hideJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  fun show(boost: Float) {
    _boostState.value = boost
    hideJob?.cancel()
    hideJob = scope.launch {
      delay(2200)
      _boostState.value = null
    }
  }

  fun hide() {
    hideJob?.cancel()
    _boostState.value = null
  }
}
