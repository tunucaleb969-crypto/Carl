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

    fun withSourceDuration(sourceDurationMs: Long): EditState =
        if (clips.isEmpty() && sourceDurationMs > 0L) EditState(listOf(Clip(sourceStartMs = 0L, sourceEndMs = sourceDurationMs))) else this

    fun reconcileSourceDuration(sourceDurationMs: Long): EditState {
        if (sourceDurationMs <= 0L) return this
        if (clips.isEmpty()) return withSourceDuration(sourceDurationMs)
        val reconciled = clips.mapNotNull { clip ->
            if (clip.sourceStartMs >= sourceDurationMs) null
            else {
                val end = clip.sourceEndMs.coerceAtMost(sourceDurationMs)
                if (end - clip.sourceStartMs >= MIN_CLIP_MS) clip.copy(sourceEndMs = end) else null
            }
        }
        return if (reconciled == clips) this else copy(clips = reconciled)
    }

    fun clipStartOnTimeline(index: Int): Long = clips.take(index).sumOf { it.durationMs }

    fun clipIndexAt(timelineMs: Long): Int {
        var elapsed = 0L
        for ((index, clip) in clips.withIndex()) {
            val end = elapsed + clip.durationMs
            if (timelineMs in elapsed until end) return index
            elapsed = end
        }
        return if (clips.isNotEmpty() && timelineMs >= elapsed) clips.size - 1 else -1
    }

    fun splitAt(timelineMs: Long): EditState {
        val index = clipIndexAt(timelineMs)
        if (index == -1) return this
        val clip = clips[index]
        val offsetIntoTimelineMs = timelineMs - clipStartOnTimeline(index)
        val offsetIntoSourceMs = (offsetIntoTimelineMs * clip.speed).toLong()
        if (offsetIntoSourceMs < MIN_CLIP_MS || clip.sourceDurationMs - offsetIntoSourceMs < MIN_CLIP_MS) return this
        val splitSourceMs = clip.sourceStartMs + offsetIntoSourceMs
        val first = clip.copy(sourceEndMs = splitSourceMs)
        val second = clip.copy(id = java.util.UUID.randomUUID().toString(), sourceStartMs = splitSourceMs)
        return copy(clips = clips.toMutableList().also {
            it[index] = first
            it.add(index + 1, second)
        })
    }

    fun indexOfClip(clipId: String): Int = clips.indexOfFirst { it.id == clipId }

    fun deleteClip(clipId: String): EditState {
        val index = indexOfClip(clipId)
        if (index == -1) return this
        return copy(clips = clips.toMutableList().also { it.removeAt(index) })
    }

    fun duplicateClip(clipId: String): EditState {
        val index = indexOfClip(clipId)
        if (index == -1) return this
        val duplicate = clips[index].copy(id = java.util.UUID.randomUUID().toString())
        return copy(clips = clips.toMutableList().also { it.add(index + 1, duplicate) })
    }

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

    fun withSpeed(clipId: String, speed: Float): EditState {
        val index = indexOfClip(clipId)
        if (index == -1 || !speed.isFinite()) return this
        val clamped = speed.coerceIn(0.25f, 4f)
        if (clips[index].speed == clamped) return this
        return copy(clips = clips.mapIndexed { clipIndex, clip ->
            if (clipIndex == index) clip.copy(speed = clamped) else clip
        })
    }

    fun withMuted(clipId: String, muted: Boolean): EditState {
        val index = indexOfClip(clipId)
        if (index == -1 || clips[index].muted == muted) return this
        return copy(clips = clips.mapIndexed { clipIndex, clip ->
            if (clipIndex == index) clip.copy(muted = muted) else clip
        })
    }

    fun updateClip(clipId: String, update: (Clip) -> Clip): EditState {
        if (indexOfClip(clipId) == -1) return this
        return copy(clips = clips.map { clip -> if (clip.id == clipId) update(clip) else clip })
    }

    companion object {
        const val MIN_CLIP_MS = 200L
    }
}
