package com.carl.editor.timeline

/**
 * The full set of clips that make up the current edit, in timeline order.
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

    /** Where clip [index] begins on the concatenated timeline (sum of every earlier clip's duration). */
    fun clipStartOnTimeline(index: Int): Long {
        return clips.take(index).sumOf { it.durationMs }
    }

    /**
     * Splits the clip under [timelineMs] into two clips at that point.
     * Returns this unchanged if the point doesn't land inside a clip, or is too close to an edge
     * to leave two valid clips (each clip must stay at least [MIN_CLIP_MS] long).
     */
    fun splitAt(timelineMs: Long): EditState {
        var elapsed = 0L
        val index = clips.indexOfFirst { clip ->
            val end = elapsed + clip.durationMs
            val hit = timelineMs in elapsed until end
            if (!hit) elapsed += clip.durationMs
            hit
        }
        if (index == -1) return this

        val clip = clips[index]
        val offsetIntoClip = timelineMs - elapsed
        if (offsetIntoClip < MIN_CLIP_MS || (clip.durationMs - offsetIntoClip) < MIN_CLIP_MS) {
            return this
        }

        val splitSourceMs = clip.sourceStartMs + offsetIntoClip
        val first = clip.copy(sourceEndMs = splitSourceMs)
        val second = clip.copy(id = java.util.UUID.randomUUID().toString(), sourceStartMs = splitSourceMs)

        val newClips = clips.toMutableList()
        newClips[index] = first
        newClips.add(index + 1, second)
        return copy(clips = newClips)
    }

    companion object {
        /** No clip may ever be shorter than this — prevents zero/negative-length clips from trims or splits. */
        const val MIN_CLIP_MS = 200L
    }
}
