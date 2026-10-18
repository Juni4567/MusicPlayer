package remix.myplayer.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Mi Music dark palette — near-black surfaces, soft gray text, blue-gray accents.
 * Splash / pattern covers still use the pink→orange brand gradient.
 */
object BrandColors {
  val Bg = Color(0xFF121212)
  val Surface = Color(0xFF1C1C1E)
  val Surface2 = Color(0xFF2A2A2E)
  val Surface3 = Color(0xFF34343A)
  val Control = Color(0xFF3F434C)
  val Search = Color(0xFF2E2E32)
  val TextPrimary = Color(0xFFF1F1EF)
  val TextSecondary = Color(0xFFB4B2B3)
  val TextMuted = Color(0xFF858588)
  val TextLabel = Color(0xFF5B5B5D)
  val Divider = Color(0xFF2F2F33)
  val BrandStart = Color(0xFFE91E8C)
  val BrandEnd = Color(0xFFFF7A45)
  val Accent = Color(0xFF7B8FA1)
  val AccentSoft = Color(0xFF5C6B7A)
  val AccentPurple = Color(0xFF603E88)

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
