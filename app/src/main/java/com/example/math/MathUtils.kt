package com.example.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Deterministic mathematical utilities for procedural geometry, analytical noise,
 * distance metrics, and harmonic synthesis.
 */
object MathUtils {

    const val TWO_PI = (2.0 * PI).toFloat()
    const val GOLDEN_RATIO = 1.618033988749895f
    const val GOLDEN_ANGLE = 2.39996322972865332f // in radians (~137.5 degrees)

    /**
     * Analytical Permutation table for fast deterministic Perlin/Simplex gradient noise.
     */
    private val P = IntArray(512) { i ->
        val perm = intArrayOf(
            151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225,
            140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148,
            247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32,
            57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175,
            74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122,
            60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54,
            65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169,
            200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64,
            52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212,
            207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213,
            119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
            129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104,
            218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241,
            81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157,
            184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93,
            222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
        )
        perm[i and 255]
    }

    private fun fade(t: Float): Float = t * t * t * (t * (t * 6f - 15f) + 10f)

    private fun lerp(t: Float, a: Float, b: Float): Float = a + t * (b - a)

    private fun grad(hash: Int, x: Float, y: Float): Float {
        val h = hash and 7
        val u = if (h < 4) x else y
        val v = if (h < 4) y else x
        return (if ((h and 1) != 0) -u else u) + (if ((h and 2) != 0) -2.0f * v else 2.0f * v)
    }

    /**
     * Classic 2D Perlin Noise normalized between -1.0 and 1.0.
     */
    fun perlin2D(x: Float, y: Float, seedOffset: Float = 0f): Float {
        val xi = floor(x + seedOffset).toInt() and 255
        val yi = floor(y + seedOffset).toInt() and 255
        val xf = (x + seedOffset) - floor(x + seedOffset)
        val yf = (y + seedOffset) - floor(y + seedOffset)

        val u = fade(xf)
        val v = fade(yf)

        val aa = P[P[xi] + yi]
        val ab = P[P[xi] + yi + 1]
        val ba = P[P[xi + 1] + yi]
        val bb = P[P[xi + 1] + yi + 1]

        val x1 = lerp(u, grad(aa, xf, yf), grad(ba, xf - 1f, yf))
        val x2 = lerp(u, grad(ab, xf, yf - 1f), grad(bb, xf - 1f, yf - 1f))
        return lerp(v, x1, x2) * 0.7f
    }

    /**
     * Fractional Brownian Motion (fBm) with configurable octaves and persistence.
     */
    fun fbm(x: Float, y: Float, octaves: Int, persistence: Float = 0.5f, lacunarity: Float = 2.0f, seed: Float = 0f): Float {
        var total = 0f
        var frequency = 1f
        var amplitude = 1f
        var maxValue = 0f

        for (i in 0 until octaves) {
            total += perlin2D(x * frequency, y * frequency, seed + i * 13.37f) * amplitude
            maxValue += amplitude
            amplitude *= persistence
            frequency *= lacunarity
        }

        return if (maxValue > 0f) total / maxValue else 0f
    }

    /**
     * Domain warped noise: f(p + g(p))
     */
    fun domainWarp(x: Float, y: Float, distortion: Float, seed: Float): Pair<Float, Float> {
        val qx = fbm(x, y, 3, seed = seed)
        val qy = fbm(x + 5.2f, y + 1.3f, 3, seed = seed + 10f)

        val rx = fbm(x + distortion * qx + 1.7f, y + distortion * qy + 9.2f, 3, seed = seed + 20f)
        val ry = fbm(x + distortion * qx + 8.3f, y + distortion * qy + 2.8f, 3, seed = seed + 30f)

        return Pair(rx, ry)
    }

    /**
     * Deterministic Pseudo-Random Number Generator based on Mulberry32 / SplitMix64.
     */
    class FastRandom(var seed: Long) {
        fun nextFloat(): Float {
            seed = (seed xor (seed ushr 30)) * -4658895280553007687L
            seed = (seed xor (seed ushr 27)) * -7723592293117605677L
            seed = seed xor (seed ushr 31)
            val bits = (seed ushr 40).toInt() and 0xFFFFFF
            return bits / 16777216.0f
        }

        fun nextFloat(min: Float, max: Float): Float = min + nextFloat() * (max - min)

        fun nextInt(bound: Int): Int {
            if (bound <= 0) return 0
            val f = nextFloat()
            return min((f * bound).toInt(), bound - 1)
        }
    }

    /**
     * Distance metrics for Voronoi and cellular automata.
     */
    enum class DistanceMetric {
        EUCLIDEAN,
        MANHATTAN,
        CHEBYSHEV,
        MINKOWSKI_HALF
    }

    fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float, metric: DistanceMetric): Float {
        val dx = abs(x1 - x2)
        val dy = abs(y1 - y2)
        return when (metric) {
            DistanceMetric.EUCLIDEAN -> sqrt(dx * dx + dy * dy)
            DistanceMetric.MANHATTAN -> dx + dy
            DistanceMetric.CHEBYSHEV -> max(dx, dy)
            DistanceMetric.MINKOWSKI_HALF -> {
                val sqrtDx = sqrt(dx)
                val sqrtDy = sqrt(dy)
                (sqrtDx + sqrtDy) * (sqrtDx + sqrtDy)
            }
        }
    }

    /**
     * Smooth clamp function.
     */
    fun clamp(value: Float, min: Float, max: Float): Float = max(min, min(value, max))
}
