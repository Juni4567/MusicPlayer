package remix.myplayer.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Brand accents and pattern/splash colors only.
 * UI surfaces, text, and dividers must come from [LocalTheme] / [AppTheme]
 * so light and dark modes stay readable.
 */
object BrandColors {
  val BrandStart = Color(0xFFE91E8C)
  val BrandEnd = Color(0xFFFF7A45)
  val Accent = Color(0xFF7B8FA1)
  val AccentSoft = Color(0xFF5C6B7A)
  val AccentPurple = Color(0xFF603E88)
  val Favorite = Color(0xFFE85D75)

  /** Default theme primary/secondary for new installs. */
  val DefaultPrimary = Accent

  fun brandGradientBrush(): Brush = Brush.verticalGradient(
    colors = listOf(BrandStart, BrandEnd)
  )

  fun patternColors(seed: Long): List<Color> {
    val palette = listOf(
      BrandStart,
      Accent,
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
