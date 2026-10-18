package remix.myplayer.ui.widget.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import remix.myplayer.R
import remix.myplayer.data.model.audio.APlayerModel
import remix.myplayer.data.model.audio.Song
import remix.myplayer.ui.theme.LocalTheme

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideCover(
  modifier: Modifier = Modifier,
  model: APlayerModel,
  circle: Boolean = true,
  album: Boolean = true
) {
  val shape = when {
    circle -> CircleShape
    else -> RoundedCornerShape(12.dp)
  }
  val seed = when (model) {
    is Song -> model.id
    else -> coverSeedFor(model.getKey())
  }
  val staticPlaceHolder =
    if (album) LocalTheme.current.albumPlaceHolder else LocalTheme.current.artistPlaceHolder

  Box(
    modifier = modifier.clip(shape)
  ) {
    if (album) {
      PatternCover(
        seed = seed,
        modifier = Modifier.fillMaxSize()
      )
    }
    GlideImage(
      model = model,
      failure = placeholder(if (album) R.drawable.transparent_placeholder else staticPlaceHolder),
      loading = placeholder(if (album) R.drawable.transparent_placeholder else staticPlaceHolder),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
  }
}
