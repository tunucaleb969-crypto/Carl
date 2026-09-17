package com.carl.editor.effects

import androidx.media3.common.Effect
import androidx.media3.effect.ScaleAndRotateTransformation

/**
 * A whole-video visual transform, applied identically to every clip.
 *
 * This is intentionally NOT per-clip: true per-clip visual effects require Media3's
 * CompositionPlayer, which is still an experimental API not present at our pinned
 * Media3 version (1.4.1). See PROJECT_STATE.md for the full tradeoff. This is a
 * deliberate, documented scope choice, not an oversight.
 */
data class GlobalTransform(
    val rotationDegrees: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false
) {
    fun rotatedClockwise(): GlobalTransform {
        return copy(rotationDegrees = (rotationDegrees + 90f) % 360f)
    }

    /** Builds the Media3 Effect list for this transform, or an empty list if it's a no-op. */
    fun toEffects(): List<Effect> {
        if (rotationDegrees == 0f && !flipHorizontal && !flipVertical) return emptyList()
        val scaleX = if (flipHorizontal) -1f else 1f
        val scaleY = if (flipVertical) -1f else 1f
        val transformation = ScaleAndRotateTransformation.Builder()
            .setScale(scaleX, scaleY)
            .setRotationDegrees(rotationDegrees)
            .build()
        return listOf(transformation)
    }
}
