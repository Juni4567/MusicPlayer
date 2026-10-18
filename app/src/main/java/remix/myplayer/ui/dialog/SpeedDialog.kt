package remix.myplayer.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import remix.myplayer.R
import remix.myplayer.ui.nav.MessageNotifier
import remix.myplayer.viewmodel.settingViewModel
import remix.myplayer.viewmodel.settings.SettingsState

@Composable
fun SpeedDialog(state: DialogState, settingState: SettingsState) {
  val settingVM = settingViewModel

  var text by rememberSaveable {
    mutableStateOf(settingState.play.speed)
  }

  InputDialog(
    dialogState = state,
    text = text,
    title = stringResource(R.string.speed),
    onValueChange = {
      text = it
    },
    onInput = {
      it.toFloatOrNull()?.let { speed ->
        if (speed > 2f || speed < 0.5f) {
          MessageNotifier.show(R.string.speed_range_tip)
          return@let
        }
        settingVM.setSpeed(it)
      }
    })
}
