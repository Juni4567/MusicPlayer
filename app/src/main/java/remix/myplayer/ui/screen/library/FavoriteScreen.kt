package remix.myplayer.ui.screen.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.R
import remix.myplayer.service.Command
import remix.myplayer.service.MusicService
import remix.myplayer.service.MusicServiceRemote.setPlayQueue
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.library.SongListHeader
import remix.myplayer.ui.widget.library.list.ListSong
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.ext.verticalScrollbar
import remix.myplayer.viewmodel.MultiSelectState
import remix.myplayer.viewmodel.libraryViewModel
import remix.myplayer.viewmodel.mainViewModel
import remix.myplayer.viewmodel.playbackViewModel

@Composable
fun FavoriteScreen() {
  val libraryVM = libraryViewModel
  val mainVM = mainViewModel
  val songs by libraryVM.favoriteSongs.collectAsStateWithLifecycle()
  val playbackState by playbackViewModel.playbackUiState.collectAsStateWithLifecycle()
  val multiSelectState by mainVM.multiSelectState.collectAsStateWithLifecycle()
  val listState = rememberLazyListState()
  val context = LocalContext.current
  val popupEnabled = !multiSelectState.isShowInLibrary()
  val theme = LocalTheme.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(theme.mainBackground)
  ) {
    if (songs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 64.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Image(
            painter = painterResource(R.drawable.widget_btn_like_nor),
            contentDescription = null,
            colorFilter = ColorFilter.tint(theme.textMuted),
            modifier = Modifier.size(56.dp)
          )
          Text(
            text = stringResource(R.string.list_is_empty),
            color = theme.textSecondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 12.dp)
          )
        }
      }
    } else {
      SongListHeader(songs)

      val selectedIds by remember {
        derivedStateOf {
          multiSelectState.selectedModels(MultiSelectState.Where.Song)
        }
      }

      LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = Modifier
          .weight(1f)
          .verticalScrollbar(listState)
      ) {
        itemsIndexed(songs, key = { _, song -> song.id }) { pos, song ->
          val selected = selectedIds.contains(song.getKey())
          val isPlayingSong = playbackState.song.id == song.id

          ListSong(
            modifier = Modifier.height(56.dp),
            song = song,
            modelParent = song,
            selected = selected,
            playing = isPlayingSong,
            popupEnabled = popupEnabled,
            onClickSong = {
              if (songs.isEmpty()) return@ListSong
              if (multiSelectState.where == MultiSelectState.Where.Song) {
                mainVM.updateMultiSelectModel(song)
                return@ListSong
              }
              setPlayQueue(
                songs,
                MusicUtil.makeCmdIntent(Command.PLAY_AT)
                  .putExtra(MusicService.EXTRA_POSITION, pos)
              )
            },
            onLongClickSong = {
              mainVM.showMultiSelect(context, MultiSelectState.Where.Song, song)
            }
          )
        }
      }
    }
  }
}
