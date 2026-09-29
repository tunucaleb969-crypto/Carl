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

    /**
     * Reconciles existing source ranges with a newly selected media asset.
     *
     * Existing edits are preserved where possible. Clips that fall completely outside the new
     * source are removed, and surviving clips are clamped to the new duration.
     */
    fun reconcileSourceDuration(sourceDurationMs: Long): EditState {
        if (sourceDurationMs <= 0L) return this
        if (clips.isEmpty()) return withSourceDuration(sourceDurationMs)

        val reconciled = clips.mapNotNull { clip ->
            if (clip.sourceStartMs >= sourceDurationMs) {
                null
            } else {
                val end = clip.sourceEndMs.coerceAtMost(sourceDurationMs)
                if (end - clip.sourceStartMs >= MIN_CLIP_MS) {
                    clip.copy(sourceEndMs = end)
                } else {
                    null
                }
            }
        }

        return if (reconciled == clips) this else copy(clips = reconciled)
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

    /** Returns the index of the clip with [clipId], or -1 when it is not on the timeline. */
    fun indexOfClip(clipId: String): Int = clips.indexOfFirst { it.id == clipId }

    /** Deletes the selected clip. Deleting the last clip is allowed and leaves an empty timeline. */
    fun deleteClip(clipId: String): EditState {
        val index = indexOfClip(clipId)
        if (index == -1) return this
        return copy(clips = clips.toMutableList().also { it.removeAt(index) })
    }

    /** Duplicates a clip immediately after itself with a new stable identity. */
    fun duplicateClip(clipId: String): EditState {
        val index = indexOfClip(clipId)
        if (index == -1) return this
        val original = clips[index]
        val duplicate = original.copy(id = java.util.UUID.randomUUID().toString())
        return copy(clips = clips.toMutableList().also { it.add(index + 1, duplicate) })
    }

    /** Moves a clip one position toward the start (-1) or end (+1) of the timeline. */
    fun moveClip(clipId: String, direction: Int): EditState {
        val index = indexOfClip(clipId)
        if (index == -1 || direction == 0) return this
        val target = index + direction.coerceIn(-1, 1)
        if (target !in clips.indices) return this
        return copy(clips = clips.toMutableList().also {
            val moved = it.removeAt(index)
            it.add(target, moved)
        })
    }

    /** Sets the playback-rate multiplier for the clip with [clipId], clamped to a sane range. */
    fun withSpeed(clipId: String, speed: Float): EditState {
        val index = indexOfClip(clipId)
        // Ignore invalid UI/programmatic input instead of allowing NaN/Infinity to reach Clip,
        // where it would throw and potentially interrupt an editing gesture.
        if (index == -1 || !speed.isFinite()) return this
        val clamped = speed.coerceIn(0.25f, 4f)
        if (clips[index].speed == clamped) return this
        return copy(clips = clips.mapIndexed { clipIndex, clip ->
            if (clipIndex == index) clip.copy(speed = clamped) else clip
        })
    }

    /** Updates one clip atomically while preserving every other timeline clip. */
    fun updateClip(clipId: String, update: (Clip) -> Clip): EditState {
        if (indexOfClip(clipId) == -1) return this
        return copy(clips = clips.map { clip ->
            if (clip.id == clipId) update(clip) else clip
        })
    }

    companion object {
        /** No clip's *source* range may ever be shorter than this — prevents zero/negative-length clips. */
        const val MIN_CLIP_MS = 200L
    }
}
