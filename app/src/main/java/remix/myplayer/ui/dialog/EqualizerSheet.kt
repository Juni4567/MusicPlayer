package remix.myplayer.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.viewmodel.settingViewModel

/**
 * Backward compatibility wrapper routing directly to the unified [PlaybackAndEqSheet].
 */
@Composable
fun EqualizerSheet(state: DialogState) {
  val settingState by settingViewModel.settingsState.collectAsStateWithLifecycle()
  PlaybackAndEqSheet(state = state, settingState = settingState, initialTab = 1)
}
