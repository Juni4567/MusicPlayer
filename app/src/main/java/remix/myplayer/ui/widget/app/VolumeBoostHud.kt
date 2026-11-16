package remix.myplayer.ui.widget.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.R
import remix.myplayer.helper.VolumeBoostHudController
import kotlin.math.roundToInt

@Composable
fun VolumeBoostHudOverlay(modifier: Modifier = Modifier) {
  val boost by VolumeBoostHudController.boostState.collectAsStateWithLifecycle()

  AnimatedVisibility(
    visible = boost != null,
    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
    modifier = modifier
      .statusBarsPadding()
      .padding(top = 16.dp)
      .fillMaxWidth()
      .wrapContentSize(Alignment.TopCenter)
  ) {
    boost?.let { b ->
      val percent = (b * 100f).roundToInt()
      val isBoosted = percent > 100

      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF222226).copy(alpha = 0.94f),
        shadowElevation = 8.dp,
        modifier = Modifier.padding(horizontal = 24.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
          Icon(
            painter = painterResource(
              if (isBoosted) R.drawable.ic_volume_up_black_24dp else R.drawable.ic_volume_down_black_24dp
            ),
            contentDescription = null,
            tint = if (isBoosted) Color(0xFFFF9800) else Color.White,
            modifier = Modifier.size(20.dp)
          )

          Text(
            text = if (isBoosted) {
              stringResource(R.string.volume_boost_active, percent)
            } else {
              "Volume: 100%"
            },
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )

          // Mini progress bar for boost range (100%..200%)
          Box(
            modifier = Modifier
              .width(80.dp)
              .height(5.dp)
              .clip(RoundedCornerShape(3.dp))
              .background(Color.White.copy(alpha = 0.25f))
          ) {
            val boostFraction = if (isBoosted) ((b - 1.0f) / 1.0f).coerceIn(0f, 1f) else 0f
            Box(
              modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(boostFraction)
                .clip(RoundedCornerShape(3.dp))
                .background(if (isBoosted) Color(0xFFFF9800) else Color.White)
            )
          }
        }
      }
    }
  }
}
