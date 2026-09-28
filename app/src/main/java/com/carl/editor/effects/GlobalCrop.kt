package com.carl.editor.effects

import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop

data class GlobalCrop(val inset: Float = 0f) {
    val normalizedInset: Float get() = inset.coerceIn(0f, 0.45f)

    @OptIn(UnstableApi::class)
    fun toEffects(): List<Effect> {
        val value = normalizedInset
        if (value <= 0f) return emptyList()
        val left = -1f + (2f * value)
        val right = 1f - (2f * value)
        val bottom = -1f + (2f * value)
        val top = 1f - (2f * value)
        return listOf(Crop(left, right, bottom, top))
    }
}
