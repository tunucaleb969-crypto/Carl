package com.carl.editor.effects

import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop

/**
 * Whole-video crop rectangle.
 *
 * Insets are normalized fractions of each edge. Each edge is clamped to 0..0.45.
 * The one-argument constructor preserves the earlier symmetric-inset API.
 */
data class GlobalCrop(
    val leftInset: Float = 0f,
    val rightInset: Float = 0f,
    val topInset: Float = 0f,
    val bottomInset: Float = 0f
) {
    constructor(inset: Float) : this(inset, inset, inset, inset)

    val normalizedLeft: Float get() = leftInset.coerceIn(0f, 0.45f)
    val normalizedRight: Float get() = rightInset.coerceIn(0f, 0.45f)
    val normalizedTop: Float get() = topInset.coerceIn(0f, 0.45f)
    val normalizedBottom: Float get() = bottomInset.coerceIn(0f, 0.45f)

    val normalizedInset: Float
        get() = if (
            normalizedLeft == normalizedRight &&
            normalizedLeft == normalizedTop &&
            normalizedLeft == normalizedBottom
        ) normalizedLeft else 0f

    private fun horizontalInsets(): Pair<Float, Float> {
        val left = normalizedLeft
        val right = normalizedRight.coerceAtMost((1f - left).coerceAtLeast(0f))
        return left to right
    }

    private fun verticalInsets(): Pair<Float, Float> {
        val top = normalizedTop
        val bottom = normalizedBottom.coerceAtMost((1f - top).coerceAtLeast(0f))
        return top to bottom
    }

    @OptIn(UnstableApi::class)
    fun toEffects(): List<Effect> {
        val (left, right) = horizontalInsets()
        val (top, bottom) = verticalInsets()
        if (left <= 0f && right <= 0f && top <= 0f && bottom <= 0f) return emptyList()
        return listOf(
            Crop(
                -1f + (2f * left),
                1f - (2f * right),
                -1f + (2f * bottom),
                1f - (2f * top)
            )
        )
    }
}
