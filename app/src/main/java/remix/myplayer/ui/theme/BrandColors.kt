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

  /**
   * 12 Curated aesthetic color schemes for procedural artwork:
   * Neon Purples, Deep Emerald Greens, Warm Sunset Oranges, Cosmic Blues,
   * Liquid Metallic, Cyber Neon, Electric Ruby, Solar Flare Gold,
   * Mystic Lavender & Mint, Cyberpunk Synthwave, Deep Ocean & Teal, Velvet Midnight & Rose.
   */
  val CuratedPalettes: List<List<Color>> = listOf(
    // 0: Neon Purples
    listOf(Color(0xFF2E1065), Color(0xFF6B21A8), Color(0xFFA855F7), Color(0xFFC084FC)),
    // 1: Deep Emerald Greens
    listOf(Color(0xFF022C22), Color(0xFF065F46), Color(0xFF10B981), Color(0xFF6EE7B7)),
    // 2: Warm Sunset Oranges
    listOf(Color(0xFF451A03), Color(0xFFC2410C), Color(0xFFEA580C), Color(0xFFFBBF24)),
    // 3: Cosmic Blues
    listOf(Color(0xFF0B1120), Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF38BDF8)),
    // 4: Liquid Metallic
    listOf(Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B), Color(0xFFCBD5E1)),
    // 5: Cyber Neon
    listOf(Color(0xFF500724), Color(0xFFBE185D), Color(0xFFF43F5E), Color(0xFF06B6D4)),
    // 6: Electric Ruby
    listOf(Color(0xFF4C0519), Color(0xFF9F1239), Color(0xFFE11D48), Color(0xFFFDA4AF)),
    // 7: Solar Flare Gold
    listOf(Color(0xFF422006), Color(0xFF854D0E), Color(0xFFCA8A04), Color(0xFFFEF08A)),
    // 8: Mystic Lavender & Mint
    listOf(Color(0xFF1E1B4B), Color(0xFF4338CA), Color(0xFF818CF8), Color(0xFFA7F3D0)),
    // 9: Cyberpunk Synthwave
    listOf(Color(0xFF2A0845), Color(0xFF6441A5), Color(0xFFFF416C), Color(0xFFFF4B2B)),
    // 10: Deep Ocean & Teal
    listOf(Color(0xFF042F2E), Color(0xFF0F766E), Color(0xFF14B8A6), Color(0xFF5EEAD4)),
    // 11: Velvet Midnight & Rose
    listOf(Color(0xFF18181B), Color(0xFF3F3F46), Color(0xFFF43F5E), Color(0xFFFB7185)),
  )

  fun patternColors(seed: Long): List<Color> {
    val paletteIndex = kotlin.math.abs((seed xor 0x3CA58F1EL).toInt()) % CuratedPalettes.size
    return CuratedPalettes[paletteIndex]
  }
}
