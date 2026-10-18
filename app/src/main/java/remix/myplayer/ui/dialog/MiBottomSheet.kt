package remix.myplayer.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import remix.myplayer.ui.theme.LocalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiBottomSheet(
  visible: Boolean,
  onDismiss: () -> Unit,
  title: String,
  content: @Composable ColumnScope.() -> Unit,
) {
  if (!visible) return

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val theme = LocalTheme.current
  val dialogBg = theme.dialogBackground
  val textPrimaryColor = theme.textPrimary
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = dialogBg,
    contentColor = textPrimaryColor,
    scrimColor = Color.Black.copy(alpha = 0.5f),
    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
    dragHandle = null,
    contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 18.dp)
        .padding(top = 10.dp, bottom = 14.dp)
    ) {
      Box(
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .width(36.dp)
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(theme.divider),
      )
      Spacer(Modifier.height(12.dp))
      Text(
        text = title,
        color = textPrimaryColor,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
      )
      content()
    }
  }
}
