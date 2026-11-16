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
      Color(0xFFE91E8C), // Vivid Pink
      Color(0xFF6366F1), // Electric Indigo
      Color(0xFF2563EB), // Royal Blue
      Color(0xFF06B6D4), // Cyan
      Color(0xFF10B981), // Emerald
      Color(0xFFF59E0B), // Warm Gold
      Color(0xFFFF7A45), // Sunset Orange
      Color(0xFF8B5CF6), // Purple
      Color(0xFFEC4899), // Deep Rose
      Color(0xFF14B8A6), // Teal
      Color(0xFF3B82F6), // Blue
      Color(0xFFA855F7), // Neon Violet
    )
    val start = ((seed ushr 1) % palette.size).toInt()
    return listOf(
      palette[start],
      palette[(start + 3) % palette.size],
      palette[(start + 6) % palette.size],
    )
  }
}
