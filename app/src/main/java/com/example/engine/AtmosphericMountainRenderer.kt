package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.math.MathUtils
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1. Atmospheric Mountain Ridges Renderer
 *
 * Procedural multi-layer horizontal mountain silhouettes generated using
 * superimposed low-frequency sine harmonics and fine tree-line noise.
 * Atmospheric Perspective: Foreground layers are deep pine green / obsidian with crisp
 * tree-line detail; background layers step up monotonically in lightness and decrease
 * in saturation, fading into an off-white misty sky.
 * Strict 6-layer depth buffer with subtle baseline haze gradients between layers.
 */
object AtmosphericMountainRenderer : WallpaperRenderer {

    override fun render(bitmap: Bitmap, params: WallpaperParams) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val canvas = Canvas(bitmap)

        val palette = params.palette
        val rng = MathUtils.FastRandom(params.seed)

        // 1. Sky Rendering with Atmospheric Mist Gradient
        val skyTopColor = palette.getColorAt(0.95f).toArgb()
        val skyHorizonColor = palette.getColorAt(0.78f).toArgb()

        val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height * 0.55f,
                skyTopColor, skyHorizonColor,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, skyPaint)

        // 2. Celestial Accent (Subtle luminous Sun / Moon disc or twilight glow)
        val showCelestial = params.subTypeIndex % 2 == 0 || params.subTypeIndex == 4
        if (showCelestial) {
            val sunX = width * (0.32f + rng.nextFloat() * 0.36f)
            val sunY = height * (0.16f + rng.nextFloat() * 0.12f)
            val sunRadius = width * 0.085f * params.scale.coerceIn(0.7f, 1.4f)

            // Celestial Outer Halo
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    sunX, sunY, sunRadius * 2.8f,
                    intArrayOf(
                        0x55FFFFFF,
                        0x22FFFFFF,
                        0x00FFFFFF
                    ),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(sunX, sunY, sunRadius * 2.8f, haloPaint)

            // Celestial Disc
            val discPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    sunX, sunY, sunRadius,
                    intArrayOf(
                        0xF0FFFFFF.toInt(),
                        palette.getColorAt(0.90f).toArgb()
                    ),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(sunX, sunY, sunRadius, discPaint)
        }

        // 3. Strict 6-Layer Atmospheric Mountain Ridges (Back to Front)
        val totalLayers = 6
        val baseYSteps = floatArrayOf(0.36f, 0.46f, 0.56f, 0.67f, 0.78f, 0.88f)

        for (layer in 0 until totalLayers) {
            val depthFraction = layer.toFloat() / (totalLayers - 1) // 0.0 (back) .. 1.0 (front)

            // Monotonic tonal ramp: back is light mist, front is deep rich obsidian/pine
            // Using reverse mapping so layer 0 is light and layer 5 is dark
            val paletteT = (1.0f - depthFraction * 0.92f).coerceIn(0.04f, 0.96f)
            val layerColor = palette.getColorAt(paletteT).toArgb()

            val baseY = height * baseYSteps[layer]
            val layerPath = buildRidgelinePath(
                width = width,
                height = height,
                baseY = baseY,
                layerIndex = layer,
                params = params
            )

            // Fill Mountain Silhouette
            val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = layerColor
            }
            canvas.drawPath(layerPath, mountainPaint)

            // Crisp outline stroke for wireframe or tactile border
            if (params.isWireframe) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = params.lineWidth * (1.2f - depthFraction * 0.4f)
                    color = 0xAAFFFFFF.toInt()
                }
                canvas.drawPath(layerPath, strokePaint)
            }

            // Baseline Atmospheric Haze Gradient (soft mist rising from base of ridge)
            if (layer < totalLayers - 1 && !params.isWireframe) {
                val hazeHeight = height * (0.08f + (1f - depthFraction) * 0.06f)
                val hazeTop = (baseY - hazeHeight * 0.3f).coerceAtLeast(0f)
                val hazeBottom = (baseY + hazeHeight * 0.7f).coerceAtMost(height)

                val mistColor = palette.getColorAt(0.92f).toArgb()
                val mistAlpha = ((1.0f - depthFraction) * 0.32f + 0.08f).coerceIn(0f, 1f)
                val hazeColorWithAlpha = (mistColor and 0x00FFFFFF) or ((mistAlpha * 255).toInt() shl 24)

                val hazePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        0f, hazeTop, 0f, hazeBottom,
                        0x00FFFFFF,
                        hazeColorWithAlpha,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, hazeTop, width, hazeBottom, hazePaint)
            }
        }
    }

    /**
     * Constructs a smooth parametric mountain ridgeline across the width of the canvas.
     */
    private fun buildRidgelinePath(
        width: Float,
        height: Float,
        baseY: Float,
        layerIndex: Int,
        params: WallpaperParams
    ): Path {
        val path = Path()
        val stepX = 4f
        val steps = (width / stepX).toInt() + 1

        val seed = params.seed + layerIndex * 1423L
        val phase1 = ((seed % 1000) * 0.015f)
        val phase2 = (((seed / 1000) % 1000) * 0.025f)
        val phase3 = (((seed / 1000000) % 1000) * 0.035f)

        // Frequency scaling: background ridges are broader and grander; foreground ridges have sharper features
        val freqBase = (1.5f + layerIndex * 0.45f) * params.scale / width * (2f * PI.toFloat())
        val amp = height * (0.075f - layerIndex * 0.006f) * params.distortion.coerceIn(0.3f, 2.0f)

        val isSharpCanyon = params.subTypeIndex == 2 || params.subTypeIndex == 3
        val isTreeline = layerIndex >= 4 && (params.subTypeIndex == 0 || params.subTypeIndex == 1)

        val startY = calculateRidgeY(0f, baseY, freqBase, amp, phase1, phase2, phase3, isSharpCanyon, isTreeline)
        path.moveTo(0f, startY)

        for (i in 1..steps) {
            val x = (i * stepX).coerceAtMost(width)
            val y = calculateRidgeY(x, baseY, freqBase, amp, phase1, phase2, phase3, isSharpCanyon, isTreeline)
            path.lineTo(x, y)
        }

        // Close to bottom of canvas
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        return path
    }

    private fun calculateRidgeY(
        x: Float,
        baseY: Float,
        freq: Float,
        amp: Float,
        p1: Float,
        p2: Float,
        p3: Float,
        sharpCanyon: Boolean,
        treeline: Boolean
    ): Float {
        // Multi-harmonic sine sum
        val h1 = sin(x * freq + p1)
        val h2 = sin(x * freq * 2.3f + p2) * 0.45f
        val h3 = sin(x * freq * 4.7f + p3) * 0.22f
        val h4 = cos(x * freq * 8.1f + p1 * 1.5f) * 0.10f

        var wave = h1 + h2 + h3 + h4

        if (sharpCanyon) {
            // Sharpen the peaks to create rugged craggy/canyon summits
            wave = (1f - 2f * abs(wave)) * 0.85f
        }

        var y = baseY + wave * amp

        // Crisp pine tree-line noise on foreground ridges
        if (treeline) {
            val treePeriod = 14f
            val treePhase = (x % treePeriod) / treePeriod
            val treePeak = (1f - abs(treePhase - 0.5f) * 2f) * 12f
            val microVariation = sin(x * 1.2f) * 4f
            y -= (treePeak + microVariation)
        }

        return y
    }
}
