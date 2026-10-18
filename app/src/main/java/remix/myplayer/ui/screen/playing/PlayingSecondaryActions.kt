package remix.myplayer.ui.screen.playing

import android.content.Intent
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
import remix.myplayer.service.Command
import remix.myplayer.ui.activity.base.BaseActivity
import remix.myplayer.ui.dialog.EqualizerSheet
import remix.myplayer.ui.dialog.SpeedDialog
import remix.myplayer.ui.dialog.rememberDialogState
import remix.myplayer.ui.nav.ExtraRestorePlayingScreen
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.RouteAudioTrim
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.theme.popupButton
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.Util
import remix.myplayer.util.Util.sendLocalBroadcast
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.PlayingScreenValue
import remix.myplayer.viewmodel.audioTrimViewModel
import remix.myplayer.viewmodel.mainViewModel
import remix.myplayer.viewmodel.playbackViewModel
import remix.myplayer.viewmodel.settingViewModel
import remix.myplayer.viewmodel.timerViewModel

/** Now Playing secondary row: favorite, trim, timer, speed, share, equalizer. */
@Composable
internal fun PlayingSecondaryActions(
  song: Song,
  @Suppress("UNUSED_PARAMETER") swatch: Palette.Swatch,
) {
  val theme = LocalTheme.current
  val activity = LocalActivity.current as? BaseActivity
  val nav = LocalNavController.current
  val scope = rememberCoroutineScope()
  val mainVM = mainViewModel
  val timerVM = timerViewModel
  val audioTrimVM = audioTrimViewModel
  val settingState by settingViewModel.settingsState.collectAsStateWithLifecycle()
  val playbackState by playbackViewModel.playbackUiState.collectAsStateWithLifecycle()
  val isFavorite = playbackState.isFavorite
  val tint = theme.popupButton()
  val canTrim = song.isLocal()
  val eqSheetState = rememberDialogState()
  val speedDialogState = rememberDialogState()
  EqualizerSheet(eqSheetState)
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
      .padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    SecondaryActionIcon(
      iconRes = if (isFavorite) {
        R.drawable.widget_btn_like_prs
      } else {
        R.drawable.widget_btn_like_nor
      },
      contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
      // Pressed asset is already red; only tint the outline/nor state.
      tint = if (isFavorite) null else tint.copy(alpha = 0.92f),
      onClick = {
        sendLocalBroadcast(MusicUtil.makeCmdIntent(Command.LOVE))
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_content_cut_24dp,
      contentDescription = if (canTrim) "Trim audio" else "Trim audio unavailable",
      tint = tint.copy(alpha = if (canTrim) 0.92f else 0.35f),
      onClick = {
        if (!canTrim) return@SecondaryActionIcon
        audioTrimVM.startTrim(song)
        navigateFromPlayingScreen(RouteAudioTrim)
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_timer_24dp,
      contentDescription = "Sleep timer",
      tint = tint.copy(alpha = 0.92f),
      onClick = { timerVM.showTimerDialog() }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_speed_24dp,
      contentDescription = "Playback speed",
      tint = tint.copy(alpha = 0.92f),
      onClick = { speedDialogState.show() }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_share_24dp,
      contentDescription = "Share",
      tint = tint.copy(alpha = 0.92f),
      onClick = {
        activity ?: return@SecondaryActionIcon
        activity.startActivity(
          Intent.createChooser(Util.createShareSongFileIntent(song, activity), null)
        )
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_eq_24dp,
      contentDescription = "Equalizer",
      tint = tint.copy(alpha = 0.92f),
      onClick = { eqSheetState.show() }
    )
  }
}

@Composable
private fun SecondaryActionIcon(
  iconRes: Int,
  contentDescription: String,
  tint: Color?,
  onClick: () -> Unit,
) {
  Image(
    painter = painterResource(iconRes),
    contentDescription = contentDescription,
    colorFilter = tint?.let { ColorFilter.tint(it) },
    modifier = Modifier
      .size(48.dp)
      .padding(12.dp)
      .clickWithRipple { onClick() }
  )
}
