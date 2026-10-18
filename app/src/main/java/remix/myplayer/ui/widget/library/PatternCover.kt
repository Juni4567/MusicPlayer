package remix.myplayer.ui.widget.library

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import remix.myplayer.ui.theme.BrandColors
import kotlin.math.abs
import kotlin.random.Random

/**
 * Deterministic geometric cover used when album art is missing.
 * Same [seed] always produces the same pattern.
 */
@Composable
fun PatternCover(
  seed: Long,
  modifier: Modifier = Modifier,
) {
  val colors = remember(seed) { BrandColors.patternColors(seed) }
  val rng = remember(seed) { Random(seed) }
  val style = remember(seed) { abs(seed.toInt()) % 3 }
  val rotation = remember(seed) { rng.nextFloat() * 40f - 20f }
  val blobOffsets = remember(seed) {
    List(4) {
      Offset(rng.nextFloat(), rng.nextFloat()) to (0.25f + rng.nextFloat() * 0.45f)
    }
  }
  val bandCount = remember(seed) { 4 + rng.nextInt(3) }

  Canvas(modifier = modifier.fillMaxSize()) {
    drawRect(
      brush = Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset(size.width, size.height)
      )
    )

    when (style) {
      0 -> {
        // Diagonal bands
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

      1 -> {
        // Soft overlapping blobs
        blobOffsets.forEachIndexed { index, (norm, scale) ->
          val radius = size.minDimension * scale
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                colors[index % colors.size].copy(alpha = 0.55f),
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

      else -> {
        // Angular chevron wedges
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
    }
  }
}

fun coverSeedFor(key: String): Long {
  var hash = 1125899906842597L
  for (ch in key) {
    hash = 31 * hash + ch.code
  }
  return hash
}
