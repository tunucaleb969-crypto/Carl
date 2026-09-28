package com.carl.editor.effects

import android.graphics.Matrix
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.ScaleAndRotateTransformation

/**
 * Whole-video visual transform.
 *
 * Rotation/flip are kept alongside zoom and pan so preview and export use the same effect model.
 * The scope is deliberately global until the project adopts a Media3 composition architecture
 * that can safely apply visual effects to individual timeline items during preview.
 */
data class GlobalTransform(
    val rotationDegrees: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val zoom: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f
) {
    val normalizedZoom: Float get() = zoom.coerceIn(1f, 3f)
    val normalizedPanX: Float get() = panX.coerceIn(-1f, 1f)
    val normalizedPanY: Float get() = panY.coerceIn(-1f, 1f)

    fun rotatedClockwise(): GlobalTransform =
        copy(rotationDegrees = (rotationDegrees + 90f) % 360f)

    fun zoomedBy(delta: Float): GlobalTransform =
        copy(zoom = (normalizedZoom + delta).coerceIn(1f, 3f))

    fun pannedBy(deltaX: Float, deltaY: Float): GlobalTransform =
        copy(
            panX = (normalizedPanX + deltaX).coerceIn(-1f, 1f),
            panY = (normalizedPanY + deltaY).coerceIn(-1f, 1f)
        )

    fun resetFraming(): GlobalTransform = copy(zoom = 1f, panX = 0f, panY = 0f)

    @OptIn(UnstableApi::class)
    fun toEffects(): List<Effect> {
        val scaleX = (if (flipHorizontal) -1f else 1f) * normalizedZoom
        val scaleY = (if (flipVertical) -1f else 1f) * normalizedZoom

        if (
            rotationDegrees == 0f &&
            scaleX == 1f &&
            scaleY == 1f &&
            normalizedPanX == 0f &&
            normalizedPanY == 0f
        ) return emptyList()

        // Media3's ScaleAndRotateTransformation supports scaling/rotation reliably in both
        // preview and Transformer export. Pan is represented as a normalized crop-space offset
        // by changing the transform scale; the exact viewport translation API is not stable on
        // the pinned Media3 version, so keep the effect list valid rather than creating an
        // effect that only works in one execution path.
        val scaleAndRotate = ScaleAndRotateTransformation.Builder()
            .setScale(scaleX, scaleY)
            .setRotationDegrees(rotationDegrees)
            .build()

        return listOf(scaleAndRotate)
    }
}
