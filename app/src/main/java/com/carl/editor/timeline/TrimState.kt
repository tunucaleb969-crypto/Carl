package com.carl.editor.timeline

data class TrimState(
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val durationMs: Long = 0L
) {
    fun withDuration(newDurationMs: Long): TrimState {
        return if (durationMs == 0L) {
            TrimState(startMs = 0L, endMs = newDurationMs, durationMs = newDurationMs)
        } else {
            this.copy(durationMs = newDurationMs)
        }
    }

    fun setStart(ms: Long): TrimState {
        val clamped = ms.coerceIn(0L, endMs - MIN_TRIM_GAP_MS)
        return copy(startMs = clamped)
    }

    fun setEnd(ms: Long): TrimState {
        val clamped = ms.coerceIn(startMs + MIN_TRIM_GAP_MS, durationMs)
        return copy(endMs = clamped)
    }

    companion object {
        const val MIN_TRIM_GAP_MS = 200L
    }
}
