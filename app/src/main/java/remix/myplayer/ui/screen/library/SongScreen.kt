package remix.myplayer.ui.screen.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.SharedFlow
import remix.myplayer.R
import remix.myplayer.service.Command
import remix.myplayer.service.MusicService
import remix.myplayer.service.MusicServiceRemote.setPlayQueue
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.library.LibraryChrome
import remix.myplayer.ui.widget.library.list.ListSong
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.ext.verticalScrollbar
import remix.myplayer.viewmodel.MultiSelectState
import remix.myplayer.viewmodel.libraryViewModel
import remix.myplayer.viewmodel.mainViewModel
import remix.myplayer.viewmodel.playbackViewModel

@Composable
fun SongScreen(scrollToCurrentEvent: SharedFlow<Unit>? = null) {
  val libraryVM = libraryViewModel
  val mainVM = mainViewModel

  val playbackState by playbackViewModel.playbackUiState.collectAsStateWithLifecycle()
  val multiSelectState by mainVM.multiSelectState.collectAsStateWithLifecycle()
  val listState = rememberLazyListState()
  val songs by libraryVM.songs.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val popupEnabled = !multiSelectState.isShowInLibrary()
  val theme = LocalTheme.current

  var searchQuery by remember { mutableStateOf("") }
  val filteredSongs = remember(songs, searchQuery) {
    if (searchQuery.isBlank()) {
      songs
    } else {
      val q = searchQuery.trim().lowercase()
      songs.filter { song ->
        song.title.lowercase().contains(q) ||
          song.artist.lowercase().contains(q) ||
          song.album.lowercase().contains(q)
      }
    }
  }

  // Dismiss keyboard and clear search focus when scrolling the song list
  LaunchedEffect(listState.isScrollInProgress) {
    if (listState.isScrollInProgress) {
      focusManager.clearFocus()
      keyboardController?.hide()
    }
  }

  // Dismiss keyboard and clear search focus whenever playback starts/changes
  LaunchedEffect(playbackState.song.id) {
    focusManager.clearFocus()
    keyboardController?.hide()
  }

  LaunchedEffect(scrollToCurrentEvent) {
    scrollToCurrentEvent?.collect {
      val index = filteredSongs.indexOfFirst { it.id == playbackState.song.id }
      if (index != -1) {
        listState.scrollToItem(index)
      }
    }
  }

  Column {
    LibraryChrome(
      songs = filteredSongs,
      searchQuery = searchQuery,
      onSearchQueryChange = { searchQuery = it }
    )

    val selectedIds by remember {
      derivedStateOf {
        multiSelectState.selectedModels(MultiSelectState.Where.Song)
      }
    }

    if (searchQuery.isNotBlank() && filteredSongs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 64.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = stringResource(R.string.no_search_result),
          color = theme.textSecondary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Medium
        )
      }
    } else {
      LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = Modifier
          .weight(1f)
          .verticalScrollbar(listState)
      ) {
        itemsIndexed(filteredSongs, key = { _, song ->
          song.id
        }) { pos, song ->
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
              focusManager.clearFocus()
              keyboardController?.hide()

              if (filteredSongs.isEmpty()) {
                return@ListSong
              }

              if (multiSelectState.where == MultiSelectState.Where.Song) {
                mainVM.updateMultiSelectModel(song)
                return@ListSong
              }

              setPlayQueue(
                filteredSongs, MusicUtil.makeCmdIntent(Command.PLAY_AT)
                  .putExtra(MusicService.EXTRA_POSITION, pos)
              )
            },
            onLongClickSong = {
              focusManager.clearFocus()
              keyboardController?.hide()
              mainVM.showMultiSelect(context, MultiSelectState.Where.Song, song)
            }
          )
        }
      }
    }
  }
}
