package com.carl.editor.effects

import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Brightness
import androidx.media3.effect.Contrast
import androidx.media3.effect.HslAdjustment

/**
 * Whole-video color adjustment, applied identically to every clip - same scope limitation and
 * same reasoning as GlobalTransform (see its kdoc): per-clip color grading would need
 * CompositionPlayer, which isn't available at our pinned Media3 version.
 */
data class ColorAdjustment(
    /** [-1, 1]. 0 = unchanged. */
    val brightness: Float = 0f,
    /** [-1, 1]. 0 = unchanged. */
    val contrast: Float = 0f,
    /** [-100, 100]. 0 = unchanged, -100 = grayscale. */
    val saturation: Float = 0f
) {
    @OptIn(UnstableApi::class)
    fun toEffects(): List<Effect> {
        val effects = mutableListOf<Effect>()
        if (brightness != 0f) effects.add(Brightness(brightness))
        if (contrast != 0f) effects.add(Contrast(contrast))
        if (saturation != 0f) {
            effects.add(HslAdjustment.Builder().adjustSaturation(saturation).build())
        }
        return effects
    }
}
