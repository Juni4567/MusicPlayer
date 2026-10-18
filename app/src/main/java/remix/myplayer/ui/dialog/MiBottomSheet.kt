package remix.myplayer.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import remix.myplayer.ui.theme.BrandColors

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
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = BrandColors.Surface,
    contentColor = BrandColors.TextPrimary,
    scrimColor = Color.Black.copy(alpha = 0.55f),
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    dragHandle = null,
    contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      Text(
        text = title,
        color = BrandColors.TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 16.dp)
      )
      content()
    }
  }
}
