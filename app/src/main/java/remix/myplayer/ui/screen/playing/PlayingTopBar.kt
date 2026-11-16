package remix.myplayer.ui.screen.playing

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import kotlinx.coroutines.launch
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.helper.SleepTimer
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.theme.popupButton
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.PlayingScreenValue
import remix.myplayer.viewmodel.mainViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.ceil

@Composable
@Stable
internal fun PlayingTopBar(song: Song, swatch: Palette.Swatch) {
  val theme = LocalTheme.current
  val titleColor = if (theme.isLight) theme.textPrimary else Color.White
  val bodyColor = if (theme.isLight) theme.textSecondary else Color.White.copy(alpha = 0.82f)
  val tintColor = if (theme.isLight) theme.textPrimary else Color.White
  val timerRunning by SleepTimer.runningState.collectAsStateWithLifecycle()
  val timerRemainMin = if (timerRunning) {
    ceil(SleepTimer.getMillisUntilFinish() / 60_000.0).toInt().coerceAtLeast(1)
  } else {
    0
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 10.dp, bottom = 4.dp)
      .heightIn(min = 56.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Spacer matching right 3-dots button size to keep title centered
    androidx.compose.foundation.layout.Spacer(
      modifier = Modifier
        .padding(8.dp)
        .size(40.dp)
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 8.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      fun cleanMeta(value: String): String {
        val sanitized = remix.myplayer.util.Util.sanitizeMeta(value)
        val trimmed = sanitized.trim()
        if (trimmed.isEmpty() ||
          trimmed.equals("download", ignoreCase = true) ||
          trimmed.equals("downloads", ignoreCase = true)
        ) {
          return ""
        }
        return trimmed
      }

      val cleanTitle = cleanMeta(song.title).ifEmpty { stringResource(R.string.unknown_song) }
      val cleanArtist = cleanMeta(song.artist)
      val cleanAlbum = cleanMeta(song.album)

      val detail = when {
        cleanArtist.isNotEmpty() && cleanAlbum.isNotEmpty() -> "$cleanArtist · $cleanAlbum"
        cleanArtist.isNotEmpty() -> cleanArtist
        cleanAlbum.isNotEmpty() -> cleanAlbum
        else -> ""
      }

      Text(
        text = cleanTitle,
        color = titleColor,
        fontSize = 17.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = Modifier.basicMarquee()
      )
      if (detail.isNotEmpty() || timerRunning) {
        val detailText = when {
          detail.isNotEmpty() && timerRunning -> "$detail · ${timerRemainMin}m"
          detail.isNotEmpty() -> detail
          else -> "${timerRemainMin}m"
        }
        Text(
          text = detailText,
          color = bodyColor,
          fontSize = 13.5.sp,
          maxLines = 1,
          textAlign = TextAlign.Center,
          modifier = Modifier.basicMarquee()
        )
      }
    }

    var expanded by remember { mutableStateOf(false) }

    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .padding(8.dp)
        .size(40.dp)
        .clickWithRipple {
          expanded = !expanded
        }
    ) {
      Image(
        painter = painterResource(R.drawable.icon_player_more),
        colorFilter = ColorFilter.tint(tintColor),
        contentDescription = "PlayingMore"
      )

      PlayingDropDownMenu(expanded, song) {
        expanded = false
      }
    }
  }
}
