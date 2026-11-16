package remix.myplayer.ui.widget.library.list

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import remix.myplayer.data.model.audio.APlayerModel
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.ui.widget.common.TextSecondary
import remix.myplayer.ui.widget.library.GlideCover
import remix.myplayer.ui.widget.popup.LibraryItemPopupButton

import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun GridItem(
  model: APlayerModel,
  text1: String,
  text2: String? = null,
  selected: Boolean,
  popupEnabled: Boolean = true,
  onClick: () -> Unit,
  onLongClick: () -> Unit
) {
  val theme = LocalTheme.current
  Column(
    modifier = Modifier
      .padding(start = 3.dp, top = 3.dp, end = 3.dp, bottom = 4.dp)
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = theme.ripple),
        onClick = { onClick() },
        onLongClick = { onLongClick() }
      )
      .background(if (selected) theme.select else theme.mainBackground)
  ) {
    GlideCover(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clip(RoundedCornerShape(10.dp)),
      model = model,
      circle = false
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier
          .padding(start = 8.dp, end = 2.dp)
          .fillMaxHeight()
          .weight(1f),
        verticalArrangement = Arrangement.Center
      ) {
        TextPrimary(text = remix.myplayer.util.Util.sanitizeMeta(text1).ifEmpty { text1 }, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
        if (text2 != null) {
          Spacer(modifier = Modifier.height(1.dp))
          TextSecondary(text = remix.myplayer.util.Util.sanitizeMeta(text2).ifEmpty { "—" }, fontSize = 11.5.sp)
        }
      }

      LibraryItemPopupButton(model = model, enabled = popupEnabled)
    }
  }
}
