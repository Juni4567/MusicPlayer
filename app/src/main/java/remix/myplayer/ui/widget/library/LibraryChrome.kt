package remix.myplayer.ui.widget.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.service.Command
import remix.myplayer.service.MusicServiceRemote.setPlayQueue
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.ext.clickWithRipple

@Composable
fun LibraryChrome(
  songs: List<Song>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit
) {
  val theme = LocalTheme.current
  val surface2 = theme.surface2
  val textPrimary = theme.textPrimary
  val textMuted = theme.textMuted
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(theme.mainBackground)
      .padding(horizontal = 14.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Search Box (in-place live filtering)
    Row(
      modifier = Modifier
        .weight(1f)
        .height(40.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(surface2)
        .padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Filled.Search,
        contentDescription = "Search",
        tint = textMuted,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
        if (searchQuery.isEmpty()) {
          Text(
            text = stringResource(R.string.search_your_library),
            color = textMuted,
            fontSize = 13.5.sp,
            maxLines = 1
          )
        }
        BasicTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          singleLine = true,
          cursorBrush = SolidColor(theme.secondary),
          textStyle = TextStyle(
            color = textPrimary,
            fontSize = 13.5.sp
          ),
          keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
          ),
          keyboardActions = KeyboardActions(
            onSearch = {
              focusManager.clearFocus()
              keyboardController?.hide()
            }
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
      if (searchQuery.isNotEmpty()) {
        Icon(
          imageVector = Icons.Filled.Clear,
          contentDescription = "Clear",
          tint = textMuted,
          modifier = Modifier
            .size(18.dp)
            .clickWithRipple(circle = true) {
              onSearchQueryChange("")
              focusManager.clearFocus()
              keyboardController?.hide()
            }
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Shuffle playback button on the same line
    Row(
      modifier = Modifier
        .height(40.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(surface2)
        .clickWithRipple(circle = false) {
          focusManager.clearFocus()
          keyboardController?.hide()
          if (songs.isNotEmpty()) {
            setPlayQueue(songs, MusicUtil.makeCmdIntent(Command.SKIP_TO_NEXT, true))
          }
        }
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_shuffle_white_24dp),
        contentDescription = "Shuffle",
        tint = theme.secondary,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = stringResource(R.string.shuffle),
        color = textPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
