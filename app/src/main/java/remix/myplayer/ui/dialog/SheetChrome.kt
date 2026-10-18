package remix.myplayer.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.LineSlider
import remix.myplayer.ui.widget.common.defaultLineSliderProperties
import remix.myplayer.util.ext.clickWithRipple

private val ChipShape = RoundedCornerShape(18.dp)

@Composable
fun SheetSectionLabel(
  text: String,
  trailing: String? = null,
  modifier: Modifier = Modifier,
) {
  val theme = LocalTheme.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(bottom = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = text,
      color = theme.textSecondary,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
    )
    if (trailing != null) {
      Text(
        text = trailing,
        color = theme.textPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
fun SheetChip(
  label: String,
  selected: Boolean,
  contentDescription: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  val theme = LocalTheme.current
  val accent = theme.secondary
  val onAccent = theme.primaryReverse
  Text(
    text = label,
    color = when {
      !enabled -> theme.textPrimary.copy(alpha = 0.35f)
      selected -> onAccent
      else -> theme.textPrimary
    },
    fontSize = 13.sp,
    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
    textAlign = TextAlign.Center,
    maxLines = 1,
    modifier = modifier
      .heightIn(min = 34.dp)
      .clip(ChipShape)
      .background(
        when {
          !enabled -> theme.surface2.copy(alpha = 0.45f)
          selected -> accent
          else -> theme.surface2
        }
      )
      .border(
        width = 1.dp,
        color = if (selected || !enabled) Color.Transparent else theme.divider,
        shape = ChipShape,
      )
      .semantics { this.contentDescription = contentDescription }
      .clickWithRipple(circle = false, enabled = enabled, onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 7.dp)
  )
}

@Composable
fun SheetSliderRow(
  label: String,
  valueLabel: String,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  steps: Int = 0,
  onValueChangeFinished: (() -> Unit)? = null,
  contentDescription: String? = null,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    SheetSectionLabel(label, trailing = valueLabel)
    LineSlider(
      modifier = Modifier
        .fillMaxWidth()
        .height(32.dp)
        .then(
          if (contentDescription != null) {
            Modifier.semantics { this.contentDescription = contentDescription }
          } else {
            Modifier
          }
        ),
      value = value,
      onValueChange = onValueChange,
      onValueChangeFinished = { onValueChangeFinished?.invoke() },
      steps = steps,
      valueRange = valueRange,
      enabled = enabled,
      properties = defaultLineSliderProperties.copy(
        trackHeight = 4.dp,
        thumbWidth = 14.dp,
        thumbHeight = 14.dp,
      ),
    )
  }
}
