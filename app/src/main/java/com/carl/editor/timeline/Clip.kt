package com.carl.editor.timeline

import java.util.UUID

/**
 * A single segment of the source video used on the timeline.
 * [sourceStartMs] / [sourceEndMs] are positions in the original source file, not on the timeline.
 * [speed] is a playback-rate multiplier for this clip only (1.0 = normal).
 * [muted] removes this clip's original audio from preview/export when true.
 */
data class Clip(
    val id: String = UUID.randomUUID().toString(),
    val sourceStartMs: Long,
    val sourceEndMs: Long,
    val speed: Float = 1f,
    val muted: Boolean = false,
    val transform: com.carl.editor.effects.GlobalTransform = com.carl.editor.effects.GlobalTransform(),
    val crop: com.carl.editor.effects.GlobalCrop = com.carl.editor.effects.GlobalCrop(),
    val color: com.carl.editor.effects.ColorAdjustment = com.carl.editor.effects.ColorAdjustment()
) {
    init {
        require(speed.isFinite() && speed > 0f) { "Clip speed must be finite and greater than zero." }
    }

    val sourceDurationMs: Long
        get() = (sourceEndMs - sourceStartMs).coerceAtLeast(0L)

    val durationMs: Long
        get() = (sourceDurationMs / speed).toLong().coerceAtLeast(0L)
}
