package com.carl.editor.timeline

import java.util.UUID

/**
 * A single segment of the source video used on the timeline.
 * [sourceStartMs] / [sourceEndMs] are positions in the original source file, not on the timeline.
 */
data class Clip(
    val id: String = UUID.randomUUID().toString(),
    val sourceStartMs: Long,
    val sourceEndMs: Long
) {
    val durationMs: Long
        get() = (sourceEndMs - sourceStartMs).coerceAtLeast(0L)
}
