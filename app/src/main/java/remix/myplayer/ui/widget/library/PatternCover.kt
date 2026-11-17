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
import androidx.compose.ui.graphics.drawscope.scale
import remix.myplayer.ui.theme.BrandColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural generative cover used when album art is missing.
 * Pre-computes and caches all geometry and brushes so scrolling in lists
 * never triggers per-frame heap allocations or garbage collection churn.
 *
 * Distinct styles:
 * 0: Constellations & Nebula Starfields
 * 1: Abstract Liquid Metallic Blobs
 * 2: Sharp Low-Poly Wireframe Crystal
 * 3: Synthwave Neon Horizon
 * 4: Isometric 3D Geometric Cube
 * 5: Sacred Geometry & Concentric Mandala
 * 6: Fluid Aurora Borealis Waves
 * 7: Orbital Planetary Rings & Cosmic Sphere
 */
@Composable
fun PatternCover(
  seed: Long,
  modifier: Modifier = Modifier,
) {
  val colors = remember(seed) { BrandColors.patternColors(seed) }
  val style = remember(seed) { abs((seed xor 0x5DEECE66DL).toInt()) % 8 }
  val rng = remember(seed) { Random(seed) }
  val rotation = remember(seed) { rng.nextFloat() * 30f - 15f }

  val bgBrush = remember(colors) {
    Brush.linearGradient(colors = colors)
  }

  // Pre-computed normalized (100x100) geometry cached per seed
  val cachedData = remember(seed, style, colors) {
    PrecomputedCoverData(seed, style, colors)
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    drawRect(brush = bgBrush)

    scale(size.width / 100f, size.height / 100f, pivot = Offset.Zero) {
      when (style) {
        0 -> drawConstellations(cachedData, colors)
        1 -> drawLiquidBlobs(cachedData, colors)
        2 -> drawCrystals(cachedData, colors, rotation)
        3 -> drawSynthwave(cachedData, colors)
        4 -> drawIsometricCube(cachedData, colors, rotation)
        5 -> drawMandala(cachedData, colors, rotation)
        6 -> drawAuroraWaves(cachedData, colors)
        else -> drawOrbitalPlanets(cachedData, colors, rotation)
      }
    }
  }
}

private class PrecomputedCoverData(seed: Long, style: Int, colors: List<Color>) {
  // Common pre-cached brushes
  val radialBrush1 = Brush.radialGradient(
    colors = listOf(colors.last().copy(alpha = 0.55f), colors.last().copy(alpha = 0f)),
    center = Offset(35f, 40f),
    radius = 55f
  )
  val radialBrush2 = Brush.radialGradient(
    colors = listOf(colors.getOrElse(1) { colors[0] }.copy(alpha = 0.45f), Color.Transparent),
    center = Offset(70f, 65f),
    radius = 50f
  )
  val sunBrush = Brush.radialGradient(
    colors = listOf(Color.White.copy(alpha = 0.95f), colors.last().copy(alpha = 0.85f), Color.Transparent),
    center = Offset(50f, 44f),
    radius = 33f
  )

  // Style 0 nodes & stars
  val constellationNodes: List<Pair<Offset, Float>>
  val starPaths: List<Path>
  val starDust: List<Offset>

  // Style 1 blob paths & brushes
  val blobPaths: List<Path>
  val blobBrushes: List<Brush>

  // Style 2 crystal facets
  val crystalFacets: List<Pair<Path, Color>>
  val crystalPoints: List<Offset>

  // Style 3 mountain paths
  val mountainPath: Path

  // Style 4 isometric cube paths
  val cubeFaces: List<Pair<Path, Color>>

  // Style 5 mandala paths
  val mandalaPetalPaths: List<Path>

  // Style 6 aurora wave paths
  val auroraWavePaths: List<Pair<Path, Brush>>

  // Style 7 orbital planet rings & paths
  val planetBrush: Brush

  init {
    val r = Random(seed)

    // Style 0
    constellationNodes = List(7) {
      Offset(15f + r.nextFloat() * 70f, 15f + r.nextFloat() * 70f) to (3f + r.nextFloat() * 4f)
    }
    starPaths = constellationNodes.map { (center, radius) ->
      buildFourPointStarPath(center, radius)
    }
    starDust = List(16) {
      Offset(r.nextFloat() * 100f, r.nextFloat() * 100f)
    }

    // Style 1
    val bPaths = mutableListOf<Path>()
    val bBrushes = mutableListOf<Brush>()
    for (i in 0 until 4) {
      val center = Offset(20f + r.nextFloat() * 60f, 20f + r.nextFloat() * 60f)
      val baseR = 15f + r.nextFloat() * 20f
      val angle = r.nextFloat() * 360f
      val path = Path()
      val steps = 8
      for (s in 0 until steps) {
        val a = (s * 2f * PI / steps) + (angle * PI / 180f)
        val radVar = baseR * (0.82f + 0.28f * sin(s * 2.2f + seed.toFloat()))
        val px = (center.x + radVar * cos(a)).toFloat()
        val py = (center.y + radVar * sin(a)).toFloat()
        if (s == 0) path.moveTo(px, py) else path.lineTo(px, py)
      }
      path.close()
      bPaths.add(path)

      val shine = listOf(
        Color.White.copy(alpha = 0.55f),
        colors[i % colors.size].copy(alpha = 0.70f),
        colors[(i + 1) % colors.size].copy(alpha = 0.20f),
        Color.Transparent
      )
      bBrushes.add(
        Brush.radialGradient(
          colors = shine,
          center = Offset(center.x - baseR * 0.3f, center.y - baseR * 0.3f),
          radius = baseR * 1.2f
        )
      )
    }
    blobPaths = bPaths
    blobBrushes = bBrushes

    // Style 2
    val cx = 50f
    val cy = 50f
    val gemW = 36f
    val gemH = 44f
    val topApex = Offset(cx, cy - gemH)
    val bottomApex = Offset(cx, cy + gemH)
    val p1 = Offset(cx - gemW, cy - gemH * 0.15f)
    val p2 = Offset(cx - gemW * 0.4f, cy + gemH * 0.10f)
    val p3 = Offset(cx + gemW * 0.4f, cy + gemH * 0.10f)
    val p4 = Offset(cx + gemW, cy - gemH * 0.15f)
    val centerNode = Offset(cx, cy - gemH * 0.05f)
    crystalPoints = listOf(topApex, bottomApex, p1, p2, p3, p4, centerNode)

    val facetDefs = listOf(
      listOf(topApex, p1, centerNode) to colors[0].copy(alpha = 0.45f),
      listOf(topApex, centerNode, p4) to colors.getOrElse(1) { colors[0] }.copy(alpha = 0.55f),
      listOf(p1, p2, centerNode) to colors.last().copy(alpha = 0.35f),
      listOf(centerNode, p2, p3) to Color.White.copy(alpha = 0.25f),
      listOf(centerNode, p3, p4) to colors.last().copy(alpha = 0.45f),
      listOf(p2, bottomApex, p3) to colors.getOrElse(1) { colors[0] }.copy(alpha = 0.60f),
      listOf(p1, bottomApex, p2) to colors[0].copy(alpha = 0.50f),
      listOf(p3, bottomApex, p4) to colors[0].copy(alpha = 0.35f)
    )
    crystalFacets = facetDefs.map { (pts, col) ->
      val p = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        lineTo(pts[1].x, pts[1].y)
        lineTo(pts[2].x, pts[2].y)
        close()
      }
      p to col
    }

    // Style 3
    mountainPath = Path().apply {
      moveTo(0f, 52f)
      lineTo(25f, 36f)
      lineTo(45f, 46f)
      lineTo(70f, 32f)
      lineTo(100f, 52f)
      close()
    }

    // Style 4
    val cubeR = 22f
    val topFace = Path().apply {
      moveTo(cx, cy - cubeR)
      lineTo(cx + cubeR * 0.866f, cy - cubeR * 0.5f)
      lineTo(cx, cy)
      lineTo(cx - cubeR * 0.866f, cy - cubeR * 0.5f)
      close()
    }
    val leftFace = Path().apply {
      moveTo(cx - cubeR * 0.866f, cy - cubeR * 0.5f)
      lineTo(cx, cy)
      lineTo(cx, cy + cubeR)
      lineTo(cx - cubeR * 0.866f, cy + cubeR * 0.5f)
      close()
    }
    val rightFace = Path().apply {
      moveTo(cx, cy)
      lineTo(cx + cubeR * 0.866f, cy - cubeR * 0.5f)
      lineTo(cx + cubeR * 0.866f, cy + cubeR * 0.5f)
      lineTo(cx, cy + cubeR)
      close()
    }
    cubeFaces = listOf(
      topFace to Color.White.copy(alpha = 0.65f),
      leftFace to colors.getOrElse(1) { colors[0] }.copy(alpha = 0.70f),
      rightFace to colors[0].copy(alpha = 0.85f)
    )

    // Style 5
    val petals = mutableListOf<Path>()
    for (i in 0 until 8) {
      val angle = (i * 45f) * PI / 180f
      val p = Path().apply {
        moveTo(50f, 50f)
        val tipX = (50f + 32f * cos(angle)).toFloat()
        val tipY = (50f + 32f * sin(angle)).toFloat()
        val midAngle1 = angle - 0.22f
        val midAngle2 = angle + 0.22f
        val c1X = (50f + 20f * cos(midAngle1)).toFloat()
        val c1Y = (50f + 20f * sin(midAngle1)).toFloat()
        val c2X = (50f + 20f * cos(midAngle2)).toFloat()
        val c2Y = (50f + 20f * sin(midAngle2)).toFloat()
        lineTo(c1X, c1Y)
        lineTo(tipX, tipY)
        lineTo(c2X, c2Y)
        close()
      }
      petals.add(p)
    }
    mandalaPetalPaths = petals

    // Style 6
    val waves = mutableListOf<Pair<Path, Brush>>()
    for (i in 0 until 4) {
      val yBase = 25f + i * 16f
      val p = Path().apply {
        moveTo(0f, 100f)
        lineTo(0f, yBase)
        for (x in 0..100 step 10) {
          val y = yBase + (10f * sin((x * 0.08f) + i + seed.toFloat())).toFloat()
          lineTo(x.toFloat(), y)
        }
        lineTo(100f, 100f)
        close()
      }
      val brush = Brush.verticalGradient(
        colors = listOf(colors[i % colors.size].copy(alpha = 0.55f), Color.Transparent),
        startY = yBase - 15f,
        endY = yBase + 35f
      )
      waves.add(p to brush)
    }
    auroraWavePaths = waves

    // Style 7
    planetBrush = Brush.radialGradient(
      colors = listOf(Color.White.copy(alpha = 0.95f), colors[0], colors.last()),
      center = Offset(42f, 42f),
      radius = 28f
    )
  }
}

private fun buildFourPointStarPath(center: Offset, radius: Float): Path = Path().apply {
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

// -------------------------------------------------------------
// Draw Helpers (zero object allocations in draw calls)
// -------------------------------------------------------------

private fun DrawScope.drawConstellations(data: PrecomputedCoverData, colors: List<Color>) {
  drawCircle(brush = data.radialBrush1, center = Offset(35f, 40f), radius = 55f)
  drawCircle(brush = data.radialBrush2, center = Offset(70f, 65f), radius = 50f)

  for (i in 0 until data.constellationNodes.size - 1) {
    drawLine(
      color = Color.White.copy(alpha = 0.35f),
      start = data.constellationNodes[i].first,
      end = data.constellationNodes[i + 1].first,
      strokeWidth = 1.0f,
      cap = StrokeCap.Round
    )
  }

  data.starPaths.forEachIndexed { idx, starPath ->
    drawPath(starPath, Color.White.copy(alpha = 0.90f))
    val center = data.constellationNodes[idx].first
    val r = data.constellationNodes[idx].second
    drawCircle(color = Color.White.copy(alpha = 0.25f), radius = r * 1.6f, center = center)
    if (idx % 2 == 0) {
      drawCircle(color = colors.last().copy(alpha = 0.35f), radius = r * 2.4f, center = center)
    }
  }

  data.starDust.forEach { pos ->
    drawCircle(color = Color.White.copy(alpha = 0.45f), radius = 1.0f, center = pos)
  }
}

private fun DrawScope.drawLiquidBlobs(data: PrecomputedCoverData, colors: List<Color>) {
  data.blobPaths.forEachIndexed { i, path ->
    drawPath(path = path, brush = data.blobBrushes[i])
    drawPath(path = path, color = Color.White.copy(alpha = 0.38f), style = Stroke(width = 1.0f))
  }
}

private fun DrawScope.drawCrystals(data: PrecomputedCoverData, colors: List<Color>, rotation: Float) {
  rotate(rotation, Offset(50f, 50f)) {
    data.crystalFacets.forEach { (path, col) ->
      drawPath(path = path, color = col)
      drawPath(path = path, color = Color.White.copy(alpha = 0.70f), style = Stroke(width = 1.1f))
    }
    data.crystalPoints.forEach { pt ->
      drawCircle(color = Color.White, radius = 2.2f, center = pt)
    }
  }
}

private fun DrawScope.drawSynthwave(data: PrecomputedCoverData, colors: List<Color>) {
  drawCircle(brush = data.sunBrush, center = Offset(50f, 44f), radius = 33f)
  drawCircle(color = colors.last(), center = Offset(50f, 44f), radius = 22f)

  drawPath(path = data.mountainPath, color = colors[0].copy(alpha = 0.75f))
  drawPath(path = data.mountainPath, color = Color.White.copy(alpha = 0.50f), style = Stroke(width = 1.0f))

  val horizonY = 52f
  for (i in 1..7) {
    val progress = i / 7f
    val y = horizonY + (progress * progress) * (100f - horizonY)
    drawLine(
      color = Color.White.copy(alpha = 0.15f + progress * 0.55f),
      start = Offset(0f, y),
      end = Offset(100f, y),
      strokeWidth = 1.0f
    )
  }

  for (i in -4..4) {
    val groundX = 50f + i * 18f
    drawLine(
      color = Color.White.copy(alpha = 0.40f),
      start = Offset(50f, horizonY),
      end = Offset(groundX, 100f),
      strokeWidth = 1.0f
    )
  }
}

private fun DrawScope.drawIsometricCube(data: PrecomputedCoverData, colors: List<Color>, rotation: Float) {
  rotate(rotation, Offset(50f, 50f)) {
    data.cubeFaces.forEach { (face, col) ->
      drawPath(path = face, color = col)
      drawPath(path = face, color = Color.White.copy(alpha = 0.75f), style = Stroke(width = 1.2f))
    }
    drawOval(
      color = Color.White.copy(alpha = 0.40f),
      topLeft = Offset(50f - 26f, 50f - 8f),
      size = Size(52f, 16f),
      style = Stroke(width = 1.2f)
    )
  }
}

private fun DrawScope.drawMandala(data: PrecomputedCoverData, colors: List<Color>, rotation: Float) {
  rotate(rotation, Offset(50f, 50f)) {
    drawCircle(
      color = Color.White.copy(alpha = 0.20f),
      radius = 42f,
      center = Offset(50f, 50f),
      style = Stroke(width = 1.0f)
    )
    drawCircle(
      color = colors.last().copy(alpha = 0.35f),
      radius = 28f,
      center = Offset(50f, 50f),
      style = Stroke(width = 1.0f)
    )
    data.mandalaPetalPaths.forEachIndexed { i, petal ->
      val col = if (i % 2 == 0) colors.first().copy(alpha = 0.45f) else colors.last().copy(alpha = 0.45f)
      drawPath(petal, col)
      drawPath(petal, Color.White.copy(alpha = 0.60f), style = Stroke(width = 0.9f))
    }
    drawCircle(color = Color.White, radius = 3.5f, center = Offset(50f, 50f))
  }
}

private fun DrawScope.drawAuroraWaves(data: PrecomputedCoverData, colors: List<Color>) {
  data.auroraWavePaths.forEach { (path, brush) ->
    drawPath(path, brush)
    drawPath(path, Color.White.copy(alpha = 0.30f), style = Stroke(width = 1.0f))
  }
}

private fun DrawScope.drawOrbitalPlanets(data: PrecomputedCoverData, colors: List<Color>, rotation: Float) {
  rotate(rotation, Offset(50f, 50f)) {
    // Backdrop stars
    data.starDust.take(8).forEach {
      drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 1.0f, center = it)
    }

    // Outer orbit
    drawOval(
      color = Color.White.copy(alpha = 0.30f),
      topLeft = Offset(12f, 32f),
      size = Size(76f, 36f),
      style = Stroke(width = 1.2f)
    )

    // Center planet
    drawCircle(
      brush = data.planetBrush,
      radius = 22f,
      center = Offset(50f, 50f)
    )

    // Front arc of orbit ring
    drawOval(
      color = Color.White.copy(alpha = 0.85f),
      topLeft = Offset(14f, 34f),
      size = Size(72f, 32f),
      style = Stroke(width = 1.4f)
    )

    // Moon satellite
    drawCircle(
      color = Color.White,
      radius = 3.5f,
      center = Offset(82f, 48f)
    )
  }
}

/**
 * 64-bit Murmur-style mixer ensuring distinct non-correlated seeds for procedural covers.
 */
fun coverSeedFor(key: String): Long {
  var hash = -0x340d631b7bdddcdaL
  for (ch in key) {
    hash = hash xor ch.code.toLong()
    hash *= 0x100000001B3L
  }
  hash = hash xor (hash ushr 33)
  hash *= -0xae502812aa7333L
  hash = hash xor (hash ushr 33)
  hash *= -0x3b314601e57a13adL
  hash = hash xor (hash ushr 33)
  return hash
}
