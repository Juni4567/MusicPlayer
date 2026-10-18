package remix.myplayer.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Mi Music brand palette from the Figma export + splash pink→orange gradient.
 */
object BrandColors {
  val Bg = Color(0xFF1B1B1B)
  val Surface = Color(0xFF222222)
  val Surface2 = Color(0xFF2E2D32)
  val Control = Color(0xFF3F434C)
  val TextPrimary = Color(0xFFF1F1EF)
  val TextSecondary = Color(0xFFB4B2B3)
  val TextMuted = Color(0xFF858588)
  val Divider = Color(0xFF323232)
  val BrandStart = Color(0xFFE91E8C)
  val BrandEnd = Color(0xFFFF7A45)
  val BrandAccent = Color(0xFFF04A6A)
  val AccentPurple = Color(0xFF603E88)

  /** Default theme primary/secondary for new installs. */
  val DefaultPrimary = BrandAccent

  fun brandGradientBrush(): Brush = Brush.verticalGradient(
    colors = listOf(BrandStart, BrandEnd)
  )

  fun patternColors(seed: Long): List<Color> {
    val palette = listOf(
      BrandStart,
      BrandAccent,
      BrandEnd,
      AccentPurple,
      Color(0xFFFF5C8A),
      Color(0xFFFF9A5C),
    )
    val start = ((seed ushr 1) % palette.size).toInt()
    return listOf(
      palette[start],
      palette[(start + 2) % palette.size],
      palette[(start + 4) % palette.size],
    )
  }
}
