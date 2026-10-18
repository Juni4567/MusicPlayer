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
import remix.myplayer.ui.dialog.rememberDialogState
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.MessageNotifier
import remix.myplayer.ui.nav.RouteTagEdit
import remix.myplayer.ui.theme.BrandColors
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.theme.popupButton
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.Util
import remix.myplayer.util.Util.sendLocalBroadcast
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.playbackViewModel
import remix.myplayer.viewmodel.tagEditViewModel
import remix.myplayer.viewmodel.timerViewModel

/** Now Playing secondary row: favorite, edit, timer, share, equalizer. */
@Composable
internal fun PlayingSecondaryActions(
  song: Song,
  @Suppress("UNUSED_PARAMETER") swatch: Palette.Swatch,
) {
  val theme = LocalTheme.current
  val activity = LocalActivity.current as? BaseActivity
  val nav = LocalNavController.current
  val timerVM = timerViewModel
  val tagEditVM = tagEditViewModel
  val playbackState by playbackViewModel.playbackUiState.collectAsStateWithLifecycle()
  val isFavorite = playbackState.isFavorite
  val tint = theme.popupButton()
  val canEditTags = song.isLocal()
  val eqSheetState = rememberDialogState()
  EqualizerSheet(eqSheetState)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 28.dp),
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
      tint = if (isFavorite) BrandColors.Favorite else tint.copy(alpha = 0.92f),
      onClick = {
        sendLocalBroadcast(MusicUtil.makeCmdIntent(Command.LOVE))
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_content_cut_24dp,
      contentDescription = if (canEditTags) "Edit tags" else "Edit tags unavailable",
      tint = tint.copy(alpha = if (canEditTags) 0.92f else 0.35f),
      onClick = {
        if (!canEditTags) {
          MessageNotifier.show(R.string.tag_edit_local_only)
          return@SecondaryActionIcon
        }
        tagEditVM.startTagEdit(song)
        nav.navigate(RouteTagEdit)
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_timer_24dp,
      contentDescription = "Sleep timer",
      tint = tint.copy(alpha = 0.92f),
      onClick = { timerVM.showTimerDialog() }
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
  tint: Color,
  onClick: () -> Unit,
) {
  Image(
    painter = painterResource(iconRes),
    contentDescription = contentDescription,
    colorFilter = ColorFilter.tint(tint),
    modifier = Modifier
      .size(48.dp)
      .padding(12.dp)
      .clickWithRipple { onClick() }
  )
}
