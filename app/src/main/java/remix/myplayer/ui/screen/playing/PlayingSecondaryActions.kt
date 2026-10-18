package remix.myplayer.ui.screen.playing

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.helper.EQHelper
import remix.myplayer.ui.activity.base.BaseActivity
import remix.myplayer.ui.dialog.SpeedDialog
import remix.myplayer.ui.dialog.rememberDialogState
import remix.myplayer.ui.nav.ExtraRestorePlayingScreen
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.RouteEq
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.PlayingScreenValue
import remix.myplayer.viewmodel.libraryViewModel
import remix.myplayer.viewmodel.mainViewModel
import remix.myplayer.viewmodel.settingViewModel
import remix.myplayer.viewmodel.timerViewModel

@Composable
internal fun PlayingSecondaryActions(
  song: Song,
  swatch: Palette.Swatch,
) {
  val tint = Color(swatch.rgb)
  val activity = LocalActivity.current as? BaseActivity
  val nav = LocalNavController.current
  val mainVM = mainViewModel
  val libraryVM = libraryViewModel
  val timerVM = timerViewModel
  val settingVM = settingViewModel
  val settingState by settingVM.settingsState.collectAsStateWithLifecycle()
  val scope = rememberCoroutineScope()

  val speedDialogState = rememberDialogState()
  SpeedDialog(speedDialogState, settingState)

  fun navigateFromPlayingScreen(route: String) {
    scope.launch {
      nav.navigate(route)
      val entry = runCatching { nav.getBackStackEntry(route) }.getOrNull() ?: return@launch
      entry.savedStateHandle[ExtraRestorePlayingScreen] = true
      entry.lifecycle.currentStateFlow.first {
        it == Lifecycle.State.RESUMED || it == Lifecycle.State.DESTROYED
      }
      if (entry.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return@launch
      }
      mainVM.playingScreenState.snapTo(PlayingScreenValue.Hidden)
      entry.lifecycle.currentStateFlow.first { it == Lifecycle.State.DESTROYED }
      mainVM.playingScreenState.snapTo(PlayingScreenValue.Expanded)
    }
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 36.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_favorite_24dp,
      contentDescription = "Favorite",
      tint = tint,
      onClick = {
        val favorite =
          libraryVM.playLists.value.firstOrNull { it.isFavorite() } ?: return@SecondaryActionIcon
        libraryVM.addSongsToPlayList(listOf(song.id), favorite.name)
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_speed_24dp,
      contentDescription = "Speed",
      tint = tint,
      onClick = { speedDialogState.show() }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_eq_24dp,
      contentDescription = "Equalizer",
      tint = tint,
      onClick = {
        activity ?: return@SecondaryActionIcon
        EQHelper.startEqualizer(activity) {
          navigateFromPlayingScreen(RouteEq)
        }
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_timer_24dp,
      contentDescription = "Sleep timer",
      tint = tint,
      onClick = { timerVM.showTimerDialog() }
    )
  }
}

@Composable
private fun SecondaryActionIcon(
  iconRes: Int,
  contentDescription: String,
  tint: Color,
  onClick: () -> Unit,
) {
  Image(
    painter = painterResource(iconRes),
    contentDescription = contentDescription,
    colorFilter = ColorFilter.tint(tint.copy(alpha = 0.85f)),
    modifier = Modifier
      .size(28.dp)
      .clickWithRipple { onClick() }
  )
}
