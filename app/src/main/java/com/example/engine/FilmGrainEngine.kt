package com.example.engine

import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.random.Random

/**
 * Hardware-accelerated Skia RuntimeShader and Bitmap export engine for Film Grain overlays.
 * Uses AGSL on Android 13+ (API 33+) with fallback to deterministic procedural pixel grain.
 */
object FilmGrainEngine {

    const val GRAIN_AGSL = """
        uniform shader composable;
        uniform float2 resolution;
        uniform float intensity;
        uniform float seed;

        float hash21(float2 p) {
            p = fract(p * float2(123.34, 456.21) + seed);
            p += dot(p, p + 45.32);
            return fract(p.x * p.y);
        }

        half4 main(float2 coord) {
            half4 color = composable.eval(coord);
            if (intensity <= 0.0) {
                return color;
            }
            float n = hash21(coord);
            float grain = (n - 0.5) * intensity * 0.38;
            half3 rgb = clamp(color.rgb + half3(grain, grain, grain), 0.0, 1.0);
            return half4(rgb, color.a);
        }
    """

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createRuntimeShader(intensity: Float, seed: Long, width: Float, height: Float): RuntimeShader {
        val shader = RuntimeShader(GRAIN_AGSL)
        shader.setFloatUniform("resolution", width.coerceAtLeast(1f), height.coerceAtLeast(1f))
        shader.setFloatUniform("intensity", intensity.coerceIn(0f, 1f))
        shader.setFloatUniform("seed", ((seed % 10000L).toFloat() / 1000f) + 0.123f)
        return shader
    }

    /**
     * Applies high-quality procedural film grain directly to Bitmap pixels (used during wallpaper export).
     */
    fun applyFilmGrainToBitmap(bitmap: Bitmap, intensity: Float, seed: Long) {
        if (intensity <= 0.005f) return
        val clampedIntensity = intensity.coerceIn(0f, 1f)
        val width = bitmap.width
        val height = bitmap.height

        val rng = Random(seed)
        val noiseAmp = (clampedIntensity * 52f).toInt()
        if (noiseAmp <= 0) return

        val rowPixels = IntArray(width)
        for (y in 0 until height) {
            bitmap.getPixels(rowPixels, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                val pixel = rowPixels[x]
                val a = (pixel ushr 24) and 0xFF
                var r = (pixel ushr 16) and 0xFF
                var g = (pixel ushr 8) and 0xFF
                var b = pixel and 0xFF

                val noise = rng.nextInt(-noiseAmp, noiseAmp + 1)
                r = (r + noise).coerceIn(0, 255)
                g = (g + noise).coerceIn(0, 255)
                b = (b + noise).coerceIn(0, 255)

                rowPixels[x] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
            bitmap.setPixels(rowPixels, 0, width, 0, y, width, 1)
        }
    }
}

/**
 * Compose modifier applying Skia RuntimeShader film grain in hardware on supported devices.
 */
fun Modifier.filmGrain(intensity: Float, seed: Long = 42L): Modifier {
    if (intensity <= 0.005f) return this
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.graphicsLayer {
            try {
                if (size.width > 0f && size.height > 0f) {
                    val shader = FilmGrainEngine.createRuntimeShader(intensity, seed, size.width, size.height)
                    renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "composable").asComposeRenderEffect()
                }
            } catch (_: Throwable) {
                // Graceful fallback if device driver lacks AGSL features
            }
        }
    } else {
        this
    }
}
