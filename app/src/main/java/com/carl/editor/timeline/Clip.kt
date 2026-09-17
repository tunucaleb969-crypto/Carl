package com.carl.editor.timeline

import java.util.UUID

/**
 * A single segment of the source video used on the timeline.
 * [sourceStartMs] / [sourceEndMs] are positions in the original source file, not on the timeline.
 * [speed] is a playback-rate multiplier for this clip only (1.0 = normal).
 */
data class Clip(
    val id: String = UUID.randomUUID().toString(),
    val sourceStartMs: Long,
    val sourceEndMs: Long,
    val speed: Float = 1f
) {
    /** How long this clip spans in the *source* media, unaffected by speed. */
    val sourceDurationMs: Long
        get() = (sourceEndMs - sourceStartMs).coerceAtLeast(0L)

    /** How long this clip occupies on the edited timeline once [speed] is applied. */
    val durationMs: Long
        get() = (sourceDurationMs / speed).toLong().coerceAtLeast(0L)
}
