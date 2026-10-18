package remix.myplayer.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.R
import remix.myplayer.helper.SleepTimer
import remix.myplayer.ui.theme.BrandColors
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.timerViewModel
import kotlin.math.ceil

private val TimerPresetsMinutes = listOf(10, 20, 30, 45, 60)

@Composable
fun TimerDialog() {
  val timerVM = timerViewModel
  val timerState by timerVM.timerState.collectAsStateWithLifecycle()
  val running by SleepTimer.runningState.collectAsStateWithLifecycle()
  val accent = LocalTheme.current.secondary

  MiBottomSheet(
    visible = timerVM.dialogState.isOpen,
    onDismiss = {
      timerVM.cancelUpdateProgress()
      timerVM.dialogState.dismiss()
    },
    title = stringResource(R.string.sleep_timer),
  ) {
    if (running) {
      val remainMin = ceil(timerState.progress / 60.0).toInt().coerceAtLeast(0)
      Text(
        text = stringResource(R.string.timer_active_remaining, remainMin),
        color = BrandColors.TextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(bottom = 12.dp)
      )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      TimerPresetsMinutes.forEach { minutes ->
        SheetActionRow(
          label = stringResource(R.string.timer_minutes, minutes),
          contentDescription = "Sleep timer $minutes minutes",
          highlighted = false,
          onClick = { timerVM.startPresetMinutes(minutes) }
        )
      }

      SheetActionRow(
        label = stringResource(R.string.timer_end_of_song),
        contentDescription = "End of current song",
        highlighted = false,
        onClick = { timerVM.startEndOfCurrentSong() }
      )

      if (running) {
        SheetActionRow(
          label = stringResource(R.string.cancel_timer),
          contentDescription = "Cancel sleep timer",
          highlighted = true,
          accent = accent,
          onClick = { timerVM.cancelActiveTimer() }
        )
      }
    }
  }
}

@Composable
private fun SheetActionRow(
  label: String,
  contentDescription: String,
  highlighted: Boolean,
  accent: androidx.compose.ui.graphics.Color = LocalTheme.current.secondary,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 48.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(if (highlighted) accent.copy(alpha = 0.18f) else BrandColors.Surface2)
      .semantics { this.contentDescription = contentDescription }
      .clickWithRipple(circle = false, onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      color = if (highlighted) accent else BrandColors.TextPrimary,
      fontSize = 15.sp,
      fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}
