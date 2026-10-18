package remix.myplayer.ui.screen.playing

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.service.Command
import remix.myplayer.ui.activity.base.BaseActivity
import remix.myplayer.ui.dialog.EqualizerSheet
import remix.myplayer.ui.dialog.SpeedDialog
import remix.myplayer.ui.dialog.rememberDialogState
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.RouteTagEdit
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.Util
import remix.myplayer.util.Util.sendLocalBroadcast
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.playbackViewModel
import remix.myplayer.viewmodel.settingViewModel
import remix.myplayer.viewmodel.tagEditViewModel
import remix.myplayer.viewmodel.timerViewModel

@Composable
internal fun PlayingSecondaryActions(
  song: Song,
  swatch: Palette.Swatch,
) {
  val tint = Color(swatch.rgb)
  val activity = LocalActivity.current as? BaseActivity
  val nav = LocalNavController.current
  val timerVM = timerViewModel
  val tagEditVM = tagEditViewModel
  val settingState by settingViewModel.settingsState.collectAsStateWithLifecycle()
  val playbackState by playbackViewModel.playbackUiState.collectAsStateWithLifecycle()
  val isFavorite = playbackState.isFavorite

  val speedDialogState = rememberDialogState()
  val eqSheetState = rememberDialogState()
  SpeedDialog(speedDialogState, settingState)
  EqualizerSheet(eqSheetState)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    SecondaryActionIcon(
      iconRes = if (isFavorite) {
        R.drawable.ic_player_favorite_filled_24dp
      } else {
        R.drawable.ic_player_favorite_24dp
      },
      contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
      tint = if (isFavorite) Color(0xFFE85D75) else tint,
      onClick = {
        sendLocalBroadcast(MusicUtil.makeCmdIntent(Command.LOVE))
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_content_cut_24dp,
      contentDescription = "Trim or edit tags",
      tint = tint,
      onClick = {
        if (song.isLocal()) {
          tagEditVM.startTagEdit(song)
          nav.navigate(RouteTagEdit)
        }
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_timer_24dp,
      contentDescription = "Sleep timer",
      tint = tint,
      onClick = { timerVM.showTimerDialog() }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_share_24dp,
      contentDescription = "Share",
      tint = tint,
      onClick = {
        activity ?: return@SecondaryActionIcon
        activity.startActivity(
          Intent.createChooser(Util.createShareSongFileIntent(song, activity), null)
        )
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_speed_24dp,
      contentDescription = "Playback speed",
      tint = tint,
      onClick = { speedDialogState.show() }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_eq_24dp,
      contentDescription = "Equalizer",
      tint = tint,
      onClick = { eqSheetState.show() }
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
    colorFilter = ColorFilter.tint(tint.copy(alpha = 0.9f)),
    modifier = Modifier
      .size(48.dp)
      .padding(12.dp)
      .clickWithRipple { onClick() }
  )
}
