package com.carl.editor.timeline

/**
 * The full set of clips that make up the current edit, in timeline order.
 * Timeline time and source time diverge once a clip's speed != 1 (see [Clip]).
 */
data class EditState(
    val clips: List<Clip> = emptyList()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.durationMs }

    /** Seeds a single full-length clip the first time we learn the source's real duration. */
    fun withSourceDuration(sourceDurationMs: Long): EditState {
        return if (clips.isEmpty() && sourceDurationMs > 0L) {
            EditState(clips = listOf(Clip(sourceStartMs = 0L, sourceEndMs = sourceDurationMs)))
        } else {
            this
        }
    }

    /** Where clip [index] begins on the concatenated timeline (sum of every earlier clip's timeline duration). */
    fun clipStartOnTimeline(index: Int): Long {
        return clips.take(index).sumOf { it.durationMs }
    }

    /** Which clip contains timeline position [timelineMs], or -1 if it's out of range. */
    fun clipIndexAt(timelineMs: Long): Int {
        var elapsed = 0L
        for ((index, clip) in clips.withIndex()) {
            val end = elapsed + clip.durationMs
            if (timelineMs in elapsed until end) return index
            elapsed = end
        }
        return if (clips.isNotEmpty() && timelineMs >= elapsed) clips.size - 1 else -1
    }

    /**
     * Splits the clip under [timelineMs] into two clips at that point.
     * Returns this unchanged if the point doesn't land inside a clip, or is too close to an edge
     * to leave two valid clips (each clip's *source* range must stay at least [MIN_CLIP_MS] long).
     */
    fun splitAt(timelineMs: Long): EditState {
        val index = clipIndexAt(timelineMs)
        if (index == -1) return this

        val clip = clips[index]
        val elapsed = clipStartOnTimeline(index)
        val offsetIntoTimelineMs = timelineMs - elapsed
        val offsetIntoSourceMs = (offsetIntoTimelineMs * clip.speed).toLong()

        if (offsetIntoSourceMs < MIN_CLIP_MS || (clip.sourceDurationMs - offsetIntoSourceMs) < MIN_CLIP_MS) {
            return this
        }

        val splitSourceMs = clip.sourceStartMs + offsetIntoSourceMs
        val first = clip.copy(sourceEndMs = splitSourceMs)
        val second = clip.copy(id = java.util.UUID.randomUUID().toString(), sourceStartMs = splitSourceMs)

        val newClips = clips.toMutableList()
        newClips[index] = first
        newClips.add(index + 1, second)
        return copy(clips = newClips)
    }

    /** Sets the playback-rate multiplier for the clip with [clipId], clamped to a sane range. */
    fun withSpeed(clipId: String, speed: Float): EditState {
        val clamped = speed.coerceIn(0.25f, 4f)
        return copy(clips = clips.map { if (it.id == clipId) it.copy(speed = clamped) else it })
    }

    companion object {
        /** No clip's *source* range may ever be shorter than this — prevents zero/negative-length clips. */
        const val MIN_CLIP_MS = 200L
    }
}
