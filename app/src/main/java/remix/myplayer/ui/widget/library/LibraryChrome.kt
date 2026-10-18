package remix.myplayer.ui.widget.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.data.model.misc.Library
import remix.myplayer.service.Command
import remix.myplayer.service.MusicServiceRemote.setPlayQueue
import remix.myplayer.ui.dialog.EqualizerSheet
import remix.myplayer.ui.dialog.rememberDialogState
import remix.myplayer.ui.nav.DetailScreenRoute
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.MessageNotifier
import remix.myplayer.ui.nav.RouteHistory
import remix.myplayer.ui.nav.RouteSearch
import remix.myplayer.ui.theme.BrandColors
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.libraryViewModel
import remix.myplayer.viewmodel.settingViewModel

@Composable
fun LibraryChrome(songs: List<Song>) {
  val nav = LocalNavController.current
  val settingVM = settingViewModel
  val libraryVM = libraryViewModel
  val eqState = rememberDialogState()
  EqualizerSheet(eqState)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(LocalTheme.current.mainBackground)
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(BrandColors.Search)
        .semantics { contentDescription = "Search your library" }
        .clickWithRipple(circle = false) { nav.navigate(RouteSearch) }
        .padding(horizontal = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Filled.Search,
        contentDescription = null,
        tint = BrandColors.TextMuted,
        modifier = Modifier.size(22.dp)
      )
      Text(
        text = stringResource(R.string.search_your_library),
        color = BrandColors.TextMuted,
        fontSize = 15.sp,
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 10.dp)
      )
      Icon(
        painter = painterResource(R.drawable.ic_player_eq_24dp),
        contentDescription = "Equalizer filter",
        tint = BrandColors.TextSecondary,
        modifier = Modifier
          .size(40.dp)
          .padding(8.dp)
          .clickWithRipple { eqState.show() }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ShortcutChip(
        label = stringResource(R.string.shortcut_recent),
        contentDescription = "Recent",
        modifier = Modifier.weight(1f),
        onClick = { nav.navigate(RouteHistory) }
      )
      ShortcutChip(
        label = stringResource(R.string.shortcut_favorites),
        contentDescription = "Favorites",
        modifier = Modifier.weight(1f),
        onClick = {
          val favorite = libraryVM.playLists.value.firstOrNull { it.isFavorite() }
          if (favorite != null) {
            nav.navigate(DetailScreenRoute(playList = favorite))
          } else {
            MessageNotifier.show(R.string.list_is_empty)
          }
        }
      )
      ShortcutChip(
        label = stringResource(R.string.shortcut_playlists),
        contentDescription = "Playlists",
        modifier = Modifier.weight(1f),
        onClick = { settingVM.requestLibraryTab(Library.TAG_PLAYLIST) }
      )
    }

    if (songs.isNotEmpty()) {
      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 48.dp)
          .clip(RoundedCornerShape(12.dp))
          .clickWithRipple(circle = false) {
            setPlayQueue(songs, MusicUtil.makeCmdIntent(Command.SKIP_TO_NEXT, true))
          }
          .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_shuffle_white_24dp),
          contentDescription = "Shuffle playback",
          tint = LocalTheme.current.secondary,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.shuffle_playback),
            color = BrandColors.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = stringResource(R.string.songs_count_tracks, songs.size),
            color = BrandColors.TextSecondary,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

@Composable
private fun ShortcutChip(
  label: String,
  contentDescription: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Text(
    text = label,
    color = BrandColors.TextPrimary,
    fontSize = 13.sp,
    fontWeight = FontWeight.Medium,
    textAlign = TextAlign.Center,
    modifier = modifier
      .heightIn(min = 40.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(BrandColors.Surface2)
      .border(1.dp, BrandColors.Divider, RoundedCornerShape(20.dp))
      .semantics { this.contentDescription = contentDescription }
      .clickWithRipple(circle = false, onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 10.dp)
  )
}
