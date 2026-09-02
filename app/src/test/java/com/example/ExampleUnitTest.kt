package com.example

import androidx.compose.ui.graphics.Color
import com.example.math.MathUtils
import com.example.palette.PaletteEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun mathUtils_noiseBounded() {
    val n = MathUtils.perlin2D(12.5f, 34.2f, 42f)
    assertTrue(n >= -1.0f && n <= 1.0f)
  }

  @Test
  fun mathUtils_distanceMetrics() {
    val dEuclidean = MathUtils.calculateDistance(0f, 0f, 3f, 4f, MathUtils.DistanceMetric.EUCLIDEAN)
    assertEquals(5f, dEuclidean, 0.001f)

    val dManhattan = MathUtils.calculateDistance(0f, 0f, 3f, 4f, MathUtils.DistanceMetric.MANHATTAN)
    assertEquals(7f, dManhattan, 0.001f)

    val dChebyshev = MathUtils.calculateDistance(0f, 0f, 3f, 4f, MathUtils.DistanceMetric.CHEBYSHEV)
    assertEquals(4f, dChebyshev, 0.001f)
  }

  @Test
  fun paletteEngine_interpolationWorks() {
    val palette = PaletteEngine.PRESET_WARM_SUNSET
    val cStart = palette.getColorAt(0f)
    val cEnd = palette.getColorAt(1f)
    assertNotNull(cStart)
    assertNotNull(cEnd)

    val hex = PaletteEngine.colorToHex(Color.Red)
    assertEquals("#FF0000", hex)
  }

  @Test
  fun paletteEngine_luminanceSorting() {
    val palette = PaletteEngine.PRESET_WARM_SUNSET
    val sorted = PaletteEngine.enforceMonotonicLuminance(palette, ascending = true)
    for (i in 0 until sorted.colors.size - 1) {
      val lumA = PaletteEngine.calculateLuminance(sorted.colors[i])
      val lumB = PaletteEngine.calculateLuminance(sorted.colors[i + 1])
      assertTrue(lumA <= lumB + 0.001f)
    }
  }
}

