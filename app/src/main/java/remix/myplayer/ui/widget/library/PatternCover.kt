package remix.myplayer.ui.widget.library

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
 * Same [seed] always produces the identical unique artwork featuring one of 5 distinct styles:
 * 0: Minimalist constellations with fluid watercolor/gradient backdrops
 * 1: Abstract liquid metallic blobs
 * 2: Sharp low-poly wireframe crystals
 * 3: Geometric neon wireframe landscapes
 * 4: Floating 3D shapes
 */
@Composable
fun PatternCover(
  seed: Long,
  modifier: Modifier = Modifier,
) {
  val colors = remember(seed) { BrandColors.patternColors(seed) }
  val rng = remember(seed) { Random(seed) }
  val style = remember(seed) { abs((seed xor 0x5DEECE66DL).toInt()) % 5 }
  val rotation = remember(seed) { rng.nextFloat() * 30f - 15f }

  // Deterministic constellation nodes
  val constellationNodes = remember(seed) {
    val r = Random(seed)
    List(7) {
      Offset(0.15f + r.nextFloat() * 0.70f, 0.15f + r.nextFloat() * 0.70f) to (6f + r.nextFloat() * 10f)
    }
  }

  // Deterministic liquid metallic blob seeds
  val blobParams = remember(seed) {
    val r = Random(seed)
    List(4) {
      Triple(
        Offset(0.2f + r.nextFloat() * 0.6f, 0.2f + r.nextFloat() * 0.6f),
        0.25f + r.nextFloat() * 0.35f,
        r.nextFloat() * 360f
      )
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    // Base backdrop gradient from palette
    drawRect(
      brush = Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset(size.width, size.height)
      )
    )

    when (style) {
      0 -> {
        // Style 0: Minimalist constellations with fluid watercolor/gradient backdrops
        // Soft fluid watercolor ambient bloom
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(colors.last().copy(alpha = 0.55f), colors.last().copy(alpha = 0f)),
            center = Offset(size.width * 0.35f, size.height * 0.40f),
            radius = size.minDimension * 0.55f
          ),
          center = Offset(size.width * 0.35f, size.height * 0.40f),
          radius = size.minDimension * 0.55f
        )
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(colors[1].copy(alpha = 0.45f), colors[1].copy(alpha = 0f)),
            center = Offset(size.width * 0.70f, size.height * 0.65f),
            radius = size.minDimension * 0.50f
          ),
          center = Offset(size.width * 0.70f, size.height * 0.65f),
          radius = size.minDimension * 0.50f
        )

        // Connecting constellation lines
        for (i in 0 until constellationNodes.size - 1) {
          val p1 = Offset(constellationNodes[i].first.x * size.width, constellationNodes[i].first.y * size.height)
          val p2 = Offset(constellationNodes[i + 1].first.x * size.width, constellationNodes[i + 1].first.y * size.height)
          drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = p1,
            end = p2,
            strokeWidth = 1.2f,
            cap = StrokeCap.Round
          )
        }

        // Star nodes and soft glows
        constellationNodes.forEachIndexed { idx, (norm, r) ->
          val center = Offset(norm.x * size.width, norm.y * size.height)
          drawFourPointStar(center, r * 1.2f, Color.White.copy(alpha = 0.90f))
          drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = r * 1.8f,
            center = center
          )
          if (idx % 2 == 0) {
            drawCircle(
              color = colors.last().copy(alpha = 0.40f),
              radius = r * 2.8f,
              center = center
            )
          }
        }

        // Micro stardust
        for (i in 0 until 16) {
          val sx = (abs(seed * (i + 1) * 37) % 1000) / 1000f * size.width
          val sy = (abs(seed * (i + 1) * 89) % 1000) / 1000f * size.height
          drawCircle(color = Color.White.copy(alpha = 0.50f), radius = 1.4f, center = Offset(sx, sy))
        }
      }

      1 -> {
        // Style 1: Abstract liquid metallic blobs
        blobParams.forEachIndexed { i, (centerNorm, radiusScale, angle) ->
          val center = Offset(centerNorm.x * size.width, centerNorm.y * size.height)
          val baseR = size.minDimension * radiusScale
          val blobPath = Path()
          val steps = 8
          for (s in 0 until steps) {
            val a = (s * 2f * PI / steps) + (angle * PI / 180f)
            val radVar = baseR * (0.82f + 0.28f * sin(s * 2.2f + seed.toFloat()))
            val px = center.x + radVar * cos(a).toFloat()
            val py = center.y + radVar * sin(a).toFloat()
            if (s == 0) blobPath.moveTo(px, py) else blobPath.lineTo(px, py)
          }
          blobPath.close()

          val metallicShine = listOf(
            Color.White.copy(alpha = 0.55f),
            colors[i % colors.size].copy(alpha = 0.70f),
            colors[(i + 1) % colors.size].copy(alpha = 0.20f),
            Color.Transparent
          )
          drawPath(
            path = blobPath,
            brush = Brush.radialGradient(
              colors = metallicShine,
              center = Offset(center.x - baseR * 0.3f, center.y - baseR * 0.3f),
              radius = baseR * 1.2f
            )
          )
          drawPath(
            path = blobPath,
            color = Color.White.copy(alpha = 0.40f),
            style = Stroke(width = 1.2f)
          )
        }
      }

      2 -> {
        // Style 2: Sharp low-poly wireframe crystals
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val gemW = size.minDimension * 0.36f
        val gemH = size.minDimension * 0.44f

        val topApex = Offset(cx, cy - gemH)
        val bottomApex = Offset(cx, cy + gemH)
        val p1 = Offset(cx - gemW, cy - gemH * 0.15f)
        val p2 = Offset(cx - gemW * 0.4f, cy + gemH * 0.10f)
        val p3 = Offset(cx + gemW * 0.4f, cy + gemH * 0.10f)
        val p4 = Offset(cx + gemW, cy - gemH * 0.15f)
        val centerNode = Offset(cx, cy - gemH * 0.05f)

        // Shaded facets
        val facets = listOf(
          listOf(topApex, p1, centerNode) to colors[0].copy(alpha = 0.45f),
          listOf(topApex, centerNode, p4) to colors[1].copy(alpha = 0.55f),
          listOf(p1, p2, centerNode) to colors.last().copy(alpha = 0.35f),
          listOf(centerNode, p2, p3) to Color.White.copy(alpha = 0.25f),
          listOf(centerNode, p3, p4) to colors.last().copy(alpha = 0.45f),
          listOf(p2, bottomApex, p3) to colors[1].copy(alpha = 0.60f),
          listOf(p1, bottomApex, p2) to colors[0].copy(alpha = 0.50f),
          listOf(p3, bottomApex, p4) to colors[0].copy(alpha = 0.35f)
        )

        rotate(rotation, Offset(cx, cy)) {
          facets.forEach { (pts, col) ->
            val path = Path().apply {
              moveTo(pts[0].x, pts[0].y)
              lineTo(pts[1].x, pts[1].y)
              lineTo(pts[2].x, pts[2].y)
              close()
            }
            drawPath(path = path, color = col)
            drawPath(path = path, color = Color.White.copy(alpha = 0.70f), style = Stroke(width = 1.4f))
          }

          listOf(topApex, bottomApex, p1, p2, p3, p4, centerNode).forEach { pt ->
            drawCircle(color = Color.White, radius = 2.8f, center = pt)
          }
        }
      }

      3 -> {
        // Style 3: Geometric neon wireframe landscapes
        val horizonY = size.height * 0.52f
        val sunCenter = Offset(size.width * 0.5f, horizonY - size.height * 0.08f)
        val sunRadius = size.minDimension * 0.22f

        // Glowing neon sun
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.95f), colors.last().copy(alpha = 0.85f), Color.Transparent),
            center = sunCenter,
            radius = sunRadius * 1.5f
          ),
          center = sunCenter,
          radius = sunRadius * 1.5f
        )
        drawCircle(
          color = colors.last(),
          center = sunCenter,
          radius = sunRadius
        )

        // Mountain ridge silhouettes in the background
        val mountain = Path().apply {
          moveTo(0f, horizonY)
          lineTo(size.width * 0.25f, horizonY - size.height * 0.16f)
          lineTo(size.width * 0.45f, horizonY - size.height * 0.06f)
          lineTo(size.width * 0.70f, horizonY - size.height * 0.20f)
          lineTo(size.width, horizonY)
          close()
        }
        drawPath(path = mountain, color = colors[0].copy(alpha = 0.75f))
        drawPath(path = mountain, color = Color.White.copy(alpha = 0.50f), style = Stroke(width = 1.2f))

        // Perspective terrain grid lines (horizontal)
        val gridLines = 7
        for (i in 1..gridLines) {
          val progress = (i.toFloat() / gridLines)
          val y = horizonY + (progress * progress) * (size.height - horizonY)
          drawLine(
            color = Color.White.copy(alpha = 0.15f + progress * 0.55f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.2f
          )
        }

        // Perspective vanishing lines
        val vanishingX = size.width * 0.5f
        for (i in -4..4) {
          val groundX = vanishingX + i * (size.width * 0.18f)
          drawLine(
            color = Color.White.copy(alpha = 0.40f),
            start = Offset(vanishingX, horizonY),
            end = Offset(groundX, size.height),
            strokeWidth = 1.2f
          )
        }
      }

      else -> {
        // Style 4: Floating 3D shapes (Isometric cubes & floating rings)
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = size.minDimension * 0.22f

        // Soft drop shadow
        drawOval(
          brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.40f), Color.Transparent),
            center = Offset(cx, cy + r * 1.5f),
            radius = r * 1.3f
          ),
          topLeft = Offset(cx - r * 1.2f, cy + r * 1.2f),
          size = androidx.compose.ui.geometry.Size(r * 2.4f, r * 0.6f)
        )

        rotate(rotation, Offset(cx, cy)) {
          // Floating 3D Isometric Cube
          val topFace = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r * 0.866f, cy - r * 0.5f)
            lineTo(cx, cy)
            lineTo(cx - r * 0.866f, cy - r * 0.5f)
            close()
          }
          val leftFace = Path().apply {
            moveTo(cx - r * 0.866f, cy - r * 0.5f)
            lineTo(cx, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r * 0.866f, cy + r * 0.5f)
            close()
          }
          val rightFace = Path().apply {
            moveTo(cx, cy)
            lineTo(cx + r * 0.866f, cy - r * 0.5f)
            lineTo(cx + r * 0.866f, cy + r * 0.5f)
            lineTo(cx, cy + r)
            close()
          }

          drawPath(path = topFace, color = Color.White.copy(alpha = 0.65f))
          drawPath(path = leftFace, color = colors[1].copy(alpha = 0.70f))
          drawPath(path = rightFace, color = colors[0].copy(alpha = 0.85f))

          drawPath(path = topFace, color = Color.White.copy(alpha = 0.80f), style = Stroke(width = 1.5f))
          drawPath(path = leftFace, color = Color.White.copy(alpha = 0.50f), style = Stroke(width = 1.5f))
          drawPath(path = rightFace, color = Color.White.copy(alpha = 0.50f), style = Stroke(width = 1.5f))

          // Orbiting rings
          drawOval(
            color = Color.White.copy(alpha = 0.40f),
            topLeft = Offset(cx - r * 1.3f, cy - r * 0.4f),
            size = androidx.compose.ui.geometry.Size(r * 2.6f, r * 0.8f),
            style = Stroke(width = 1.4f)
          )
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
