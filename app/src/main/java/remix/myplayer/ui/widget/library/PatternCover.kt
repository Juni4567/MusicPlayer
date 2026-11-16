package remix.myplayer.ui.widget.library

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import remix.myplayer.ui.theme.BrandColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural generative cover used when album art is missing.
 * Same [seed] always produces the identical unique artwork featuring textures,
 * vector geometry, stars, soundwaves, grids, and harmonic waves.
 */
@Composable
fun PatternCover(
  seed: Long,
  modifier: Modifier = Modifier,
) {
  val colors = remember(seed) { BrandColors.patternColors(seed) }
  val rng = remember(seed) { Random(seed) }
  val style = remember(seed) { abs((seed xor 0x5DEECE66DL).toInt()) % 8 }
  val rotation = remember(seed) { rng.nextFloat() * 40f - 20f }
  val bandCount = remember(seed) { 4 + rng.nextInt(3) }

  // Pre-generate deterministic star & constellation points
  val starPoints = remember(seed) {
    val r = Random(seed)
    List(7) {
      Triple(
        Offset(0.12f + r.nextFloat() * 0.76f, 0.12f + r.nextFloat() * 0.76f),
        8f + r.nextFloat() * 14f,
        r.nextInt(2) == 0
      )
    }
  }

  // Pre-generate blobs
  val blobOffsets = remember(seed) {
    val r = Random(seed)
    List(4) {
      Offset(r.nextFloat(), r.nextFloat()) to (0.28f + r.nextFloat() * 0.42f)
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    // Rich background gradient base
    drawRect(
      brush = Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset(size.width, size.height)
      )
    )

    when (style) {
      0 -> {
        // Style 0: Celestial Constellations & Twinkling Stars
        val starColor = Color.White
        // Constellation lines connecting stars
        for (i in 0 until starPoints.size - 1) {
          val p1 = Offset(starPoints[i].first.x * size.width, starPoints[i].first.y * size.height)
          val p2 = Offset(starPoints[i + 1].first.x * size.width, starPoints[i + 1].first.y * size.height)
          drawLine(
            color = starColor.copy(alpha = 0.25f),
            start = p1,
            end = p2,
            strokeWidth = 1.2f,
            cap = StrokeCap.Round
          )
        }
        // Draw 4-pointed glowing stars and particles
        starPoints.forEach { (norm, r, isBig) ->
          val center = Offset(norm.x * size.width, norm.y * size.height)
          val starRadius = if (isBig) r * 1.3f else r
          drawFourPointStar(center, starRadius, starColor.copy(alpha = 0.85f))
          drawCircle(
            color = starColor.copy(alpha = 0.35f),
            radius = starRadius * 1.8f,
            center = center
          )
        }
        // Micro stardust
        for (i in 0 until 18) {
          val sx = (abs(seed * (i + 1) * 31) % 1000) / 1000f * size.width
          val sy = (abs(seed * (i + 1) * 73) % 1000) / 1000f * size.height
          drawCircle(color = starColor.copy(alpha = 0.45f), radius = 1.5f, center = Offset(sx, sy))
        }
      }

      1 -> {
        // Style 1: Concentric Soundwave Rings & Resonance Waves
        val center = Offset(size.width * 0.48f, size.height * 0.52f)
        val maxR = size.maxDimension * 0.65f
        val ringCount = 7
        for (i in 1..ringCount) {
          val radius = (maxR / ringCount) * i
          val stroke = if (i % 2 == 0) 2.2f else 1.2f
          val alpha = 0.15f + (ringCount - i) * 0.06f
          drawCircle(
            color = Color.White.copy(alpha = alpha.coerceIn(0.1f, 0.6f)),
            center = center,
            radius = radius,
            style = Stroke(width = stroke)
          )
        }
        // Central pulse dot
        drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 6f, center = center)
        drawCircle(color = Color.White.copy(alpha = 0.25f), radius = 14f, center = center)
      }

      2 -> {
        // Style 2: High-Tech Vector Grid & Crosshatch Geometry
        rotate(rotation) {
          val step = size.maxDimension / 9f
          for (i in -2..11) {
            val offset = i * step
            drawLine(
              color = Color.White.copy(alpha = 0.22f),
              start = Offset(offset, -size.height * 0.5f),
              end = Offset(offset, size.height * 1.5f),
              strokeWidth = 1.2f
            )
            drawLine(
              color = Color.White.copy(alpha = 0.15f),
              start = Offset(-size.width * 0.5f, offset),
              end = Offset(size.width * 1.5f, offset),
              strokeWidth = 1.2f
            )
          }
          // Center diamond accent
          val diamond = Path().apply {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val d = size.minDimension * 0.28f
            moveTo(cx, cy - d)
            lineTo(cx + d, cy)
            lineTo(cx, cy + d)
            lineTo(cx - d, cy)
            close()
          }
          drawPath(path = diamond, color = Color.White.copy(alpha = 0.20f))
          drawPath(path = diamond, color = Color.White.copy(alpha = 0.60f), style = Stroke(width = 2f))
        }
      }

      3 -> {
        // Style 3: Cosmic Glowing Blobs & Chromatic Aura
        blobOffsets.forEachIndexed { index, (norm, scale) ->
          val radius = size.minDimension * scale
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                colors[index % colors.size].copy(alpha = 0.60f),
                colors[index % colors.size].copy(alpha = 0f)
              ),
              center = Offset(norm.x * size.width, norm.y * size.height),
              radius = radius
            ),
            radius = radius,
            center = Offset(norm.x * size.width, norm.y * size.height)
          )
        }
      }

      4 -> {
        // Style 4: Angular Chevron Wedges & Crystal Shards
        rotate(rotation) {
          for (i in 0 until bandCount) {
            val path = Path().apply {
              val x0 = -size.width * 0.2f + i * size.width / bandCount
              moveTo(x0, 0f)
              lineTo(x0 + size.width * 0.35f, size.height / 2f)
              lineTo(x0, size.height)
              close()
            }
            drawPath(
              path = path,
              color = colors[i % colors.size].copy(alpha = 0.32f)
            )
          }
        }
      }

      5 -> {
        // Style 5: Sonic Equalizer Waves & Sine Curves
        val waveCount = 3
        for (w in 0 until waveCount) {
          val path = Path()
          val baseline = size.height * (0.35f + w * 0.18f)
          val amplitude = size.height * (0.08f + w * 0.03f)
          val freq = (1.5f + w * 0.7f) * (2f * PI.toFloat() / size.width)
          val phase = (seed % 100) * 0.05f + w * 1.2f

          path.moveTo(0f, baseline + amplitude * sin(phase))
          var x = 0f
          while (x <= size.width) {
            val y = baseline + amplitude * sin(freq * x + phase)
            path.lineTo(x, y)
            x += 8f
          }
          drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.35f - w * 0.08f),
            style = Stroke(width = 2.5f - w * 0.5f, cap = StrokeCap.Round)
          )
        }
      }

      6 -> {
        // Style 6: Radial Burst Starlight Rays
        val rayCenter = Offset(size.width * 0.5f, size.height * 0.5f)
        val numRays = 16
        val rayAngle = (2f * PI / numRays).toFloat()
        val rayLength = size.maxDimension * 0.9f
        for (i in 0 until numRays) {
          if (i % 2 == 0) {
            val a1 = i * rayAngle
            val a2 = (i + 1) * rayAngle
            val path = Path().apply {
              moveTo(rayCenter.x, rayCenter.y)
              lineTo(rayCenter.x + rayLength * cos(a1), rayCenter.y + rayLength * sin(a1))
              lineTo(rayCenter.x + rayLength * cos(a2), rayCenter.y + rayLength * sin(a2))
              close()
            }
            drawPath(path = path, color = Color.White.copy(alpha = 0.12f))
          }
        }
        drawCircle(color = Color.White.copy(alpha = 0.40f), radius = size.minDimension * 0.12f, center = rayCenter)
      }

      else -> {
        // Style 7: Diagonal Texture Bands & Rhythm Bars
        rotate(rotation) {
          val bandWidth = size.maxDimension / bandCount
          for (i in -1 until bandCount + 2) {
            val color = colors[i.mod(colors.size)].copy(alpha = 0.28f)
            drawRect(
              color = color,
              topLeft = Offset(i * bandWidth - size.width * 0.2f, -size.height * 0.3f),
              size = Size(bandWidth * 0.55f, size.height * 1.6f)
            )
          }
        }
      }
    }
  }
}

private fun DrawScope.drawFourPointStar(center: Offset, radius: Float, color: Color) {
  val path = Path().apply {
    val inner = radius * 0.25f
    moveTo(center.x, center.y - radius)
    lineTo(center.x + inner, center.y - inner)
    lineTo(center.x + radius, center.y)
    lineTo(center.x + inner, center.y + inner)
    lineTo(center.x, center.y + radius)
    lineTo(center.x - inner, center.y + inner)
    lineTo(center.x - radius, center.y)
    lineTo(center.x - inner, center.y - inner)
    close()
  }
  drawPath(path = path, color = color)
}

fun coverSeedFor(key: String): Long {
  var hash = 1125899906842597L
  for (ch in key) {
    hash = 31 * hash + ch.code
  }
  return hash
}
