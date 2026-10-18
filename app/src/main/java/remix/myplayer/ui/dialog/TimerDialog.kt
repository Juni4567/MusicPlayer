package remix.myplayer.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.viewmodel.timerViewModel
import kotlin.math.ceil

private val TimerPresetsMinutes = listOf(10, 20, 30, 45, 60)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerDialog() {
  val timerVM = timerViewModel
  val timerState by timerVM.timerState.collectAsStateWithLifecycle()
  val running by SleepTimer.runningState.collectAsStateWithLifecycle()
  val theme = LocalTheme.current
  val accent = theme.secondary

  MiBottomSheet(
    visible = timerVM.dialogState.isOpen,
    onDismiss = {
      timerVM.cancelUpdateProgress()
      timerVM.dialogState.dismiss()
    },
    title = stringResource(R.string.sleep_timer),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (running) {
        val remainMin = ceil(timerState.progress / 60.0).toInt().coerceAtLeast(0)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.14f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.timer_active_remaining, remainMin),
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
          )
          Text(
            text = stringResource(R.string.cancel_timer),
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
              .semantics { contentDescription = "Cancel sleep timer" }
              .clickWithRipple(circle = false) { timerVM.cancelActiveTimer() }
              .padding(4.dp),
          )
        }
      }

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        TimerPresetsMinutes.forEach { minutes ->
          SheetChip(
            label = stringResource(R.string.timer_minutes, minutes),
            selected = false,
            contentDescription = "Sleep timer $minutes minutes",
            onClick = { timerVM.startPresetMinutes(minutes) },
          )
        }
        SheetChip(
          label = stringResource(R.string.timer_end_of_song),
          selected = false,
          contentDescription = "End of current song",
          onClick = { timerVM.startEndOfCurrentSong() },
        )
      }
    }
  }
}
