package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.engine.AspectRatioPreset
import com.example.engine.ProceduralRenderer
import com.example.engine.WallpaperParams
import com.example.engine.WallpaperPatternType
import com.example.palette.PaletteEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Wallpaper Studio", appName)
  }

  @Test
  fun `all 5 procedural generators render valid bitmaps`() = runBlocking {
    for (type in WallpaperPatternType.entries) {
      for (subType in type.subTypes.indices) {
        val params = WallpaperParams(
          patternType = type,
          subTypeIndex = subType,
          seed = 42L,
          scale = 1.0f,
          complexity = 1.0f
        )
        val bitmap = ProceduralRenderer.renderToBitmap(120, 240, params)
        assertNotNull("Bitmap must not be null for ${type.name} subtype $subType", bitmap)
        assertEquals(120, bitmap.width)
        assertEquals(240, bitmap.height)
        bitmap.recycle()
      }
    }
  }

  @Test
  fun `algorithmic color palettes generate valid contrast stops`() {
    val base = Color(0xFF6750A4)
    val complementary = PaletteEngine.generateComplementary(base)
    val triadic = PaletteEngine.generateTriadic(base)
    val monochromatic = PaletteEngine.generateMonochromatic(base)
    val analogous = PaletteEngine.generateAnalogous(base)

    assertTrue(complementary.colors.size >= 4)
    assertTrue(triadic.colors.size >= 4)
    assertTrue(monochromatic.colors.size >= 4)
    assertTrue(analogous.colors.size >= 4)
  }

  @Test
  fun `monotonic luminance sorting orders colors consistently`() {
    val palette = PaletteEngine.PRESET_WARM_SUNSET
    val sortedAscending = PaletteEngine.enforceMonotonicLuminance(palette, ascending = true)
    val sortedDescending = PaletteEngine.enforceMonotonicLuminance(palette, ascending = false)

    assertEquals(palette.colors.size, sortedAscending.colors.size)
    assertEquals(palette.colors.size, sortedDescending.colors.size)
  }

  @Test
  fun `aspect ratio calculations are mathematically accurate`() {
    val tall = AspectRatioPreset.PHONE_TALL
    assertEquals(9f / 20f, tall.ratio, 0.001f)
    assertEquals(1080, tall.defaultExportWidth)
    assertEquals(2400, tall.defaultExportHeight)
  }
}
