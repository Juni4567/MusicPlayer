package remix.myplayer.ui.widget.library.list

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import remix.myplayer.data.model.audio.APlayerModel
import remix.myplayer.data.model.audio.Song
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.theme.highLightText
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.ui.widget.common.TextSecondary
import remix.myplayer.ui.widget.library.GlideCover
import remix.myplayer.ui.widget.popup.SongPopupButton

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ListSong(
  modifier: Modifier = Modifier,
  song: Song,
  modelParent: APlayerModel,
  selected: Boolean,
  playing: Boolean,
  popupEnabled: Boolean = true,
  onClickSong: () -> Unit,
  onLongClickSong: () -> Unit,
  num: Int? = null,
) {
  val theme = LocalTheme.current

  Box(
    modifier = modifier
      .fillMaxWidth()
      .combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = theme.ripple),
        onClick = { onClickSong() },
        onLongClick = { onLongClickSong() }
      )
      .background(if (selected) theme.select else theme.mainBackground),
    contentAlignment = Alignment.CenterStart
  ) {
    if (playing) {
      Box(
        modifier = Modifier
          .width(3.5.dp)
          .fillMaxHeight()
          .padding(vertical = 10.dp)
          .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
          .background(theme.highLightText())
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (num != null) {
        TextPrimary(
          text = if (num > 999) "999+" else num.toString(),
          textAlign = TextAlign.Center,
          fontSize = 13.sp,
          color = if (playing) theme.highLightText() else theme.textSecondary,
          modifier = Modifier
            .width(36.dp)
            .padding(horizontal = 2.dp)
        )
      } else {
        Spacer(modifier = Modifier.width(12.dp))
      }

      GlideCover(
        model = song,
        circle = false,
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(8.dp))
      )

      Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
          .weight(1f)
          .padding(start = 12.dp, end = 6.dp)
      ) {
        TextPrimary(
          text = song.showName,
          fontSize = 14.5.sp,
          fontWeight = if (playing) FontWeight.SemiBold else FontWeight.Medium,
          color = if (playing) theme.highLightText() else theme.textPrimary
        )
        if (song.cleanArtist.isNotEmpty()) {
          Spacer(modifier = Modifier.height(2.dp))
          TextSecondary(
            text = song.cleanArtist,
            fontSize = 12.sp
          )
        }
      }

      SongPopupButton(
        modifier = Modifier,
        song = song,
        parent = modelParent,
        enabled = popupEnabled
      )
    }
  }

}
