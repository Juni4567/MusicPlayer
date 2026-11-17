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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import remix.myplayer.service.MusicServiceRemote
import androidx.compose.ui.res.stringResource
import remix.myplayer.ui.activity.base.BaseActivity
import remix.myplayer.ui.dialog.PlaybackAndEqSheet
import remix.myplayer.ui.dialog.rememberDialogState
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.theme.popupButton
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.Util
import remix.myplayer.util.Util.sendLocalBroadcast
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.playbackViewModel
import remix.myplayer.viewmodel.settingViewModel
import remix.myplayer.viewmodel.timerViewModel

/** Now Playing secondary row: favorite, lyrics, timer, audio & equalizer, share. */
@Composable
internal fun PlayingSecondaryActions(
  song: Song,
  @Suppress("UNUSED_PARAMETER") swatch: Palette.Swatch,
  showLyrics: Boolean = false,
  onToggleLyrics: () -> Unit = {}
) {
  val theme = LocalTheme.current
  val settingVM = settingViewModel
  val timerVM = timerViewModel
  val settingState by settingVM.settingsState.collectAsStateWithLifecycle()
  val playbackState by playbackViewModel.playbackUiState.collectAsStateWithLifecycle()
  val isFavorite = playbackState.isFavorite
  val tint = theme.popupButton()

  val audioSheetState = rememberDialogState()
  var initialTab by remember { mutableIntStateOf(0) }
  PlaybackAndEqSheet(
    state = audioSheetState,
    settingState = settingState,
    initialTab = initialTab
  )

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
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
      iconRes = R.drawable.ic_lyrics_24dp,
      contentDescription = "Lyrics",
      tint = if (showLyrics) theme.secondary else tint.copy(alpha = 0.92f),
      onClick = onToggleLyrics
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_timer_24dp,
      contentDescription = "Sleep timer",
      tint = tint.copy(alpha = 0.92f),
      onClick = { timerVM.showTimerDialog() }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_player_eq_24dp,
      contentDescription = "Sound effects and equalizer",
      tint = tint.copy(alpha = 0.92f),
      onClick = {
        initialTab = 0
        audioSheetState.show()
      }
    )
    SecondaryActionIcon(
      iconRes = R.drawable.ic_refresh_24dp,
      contentDescription = stringResource(R.string.mood_reset),
      tint = tint.copy(alpha = 0.92f),
      onClick = {
        MusicServiceRemote.service?.playback?.speed = 1.0f
        MusicServiceRemote.service?.playback?.pitch = 1.0f
        settingVM.setSpeed("1.0")
        settingVM.setPitch("1.0")
        settingVM.setEchoEnabled(false)
      }
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
      .size(44.dp)
      .padding(10.dp)
      .clickWithRipple { onClick() }
  )
}
