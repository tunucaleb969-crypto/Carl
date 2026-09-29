package com.carl.editor

import com.carl.editor.canvas.CanvasSettings
import com.carl.editor.effects.ColorAdjustment
import com.carl.editor.effects.GlobalCrop
import com.carl.editor.effects.GlobalTransform
import com.carl.editor.timeline.Clip
import com.carl.editor.timeline.EditState

/**
 * Single immutable snapshot of the editable project.
 *
 * Keeping timeline and visual/canvas settings together makes undo/redo atomic: one history
 * operation represents one coherent project state instead of independent Compose state buckets.
 */
data class EditorProjectState(
    val timeline: EditState = EditState(),
    val globalTransform: GlobalTransform = GlobalTransform(),
    val globalCrop: GlobalCrop = GlobalCrop(),
    val colorAdjustment: ColorAdjustment = ColorAdjustment(),
    val canvasSettings: CanvasSettings = CanvasSettings()
) {
    val clips: List<Clip> get() = timeline.clips
    val totalDurationMs: Long get() = timeline.totalDurationMs

    fun withSourceDuration(sourceDurationMs: Long): EditorProjectState =
        copy(timeline = timeline.withSourceDuration(sourceDurationMs))

    /** Reconciles the current timeline against the duration of the selected source. */
    fun reconcileSourceDuration(sourceDurationMs: Long): EditorProjectState =
        copy(timeline = timeline.reconcileSourceDuration(sourceDurationMs))

    fun clipStartOnTimeline(index: Int): Long = timeline.clipStartOnTimeline(index)
    fun clipIndexAt(timelineMs: Long): Int = timeline.clipIndexAt(timelineMs)
    fun indexOfClip(clipId: String): Int = timeline.indexOfClip(clipId)

    fun splitAt(timelineMs: Long): EditorProjectState =
        copy(timeline = timeline.splitAt(timelineMs))

    fun deleteClip(clipId: String): EditorProjectState =
        copy(timeline = timeline.deleteClip(clipId))

    fun duplicateClip(clipId: String): EditorProjectState =
        copy(timeline = timeline.duplicateClip(clipId))

    fun moveClip(clipId: String, direction: Int): EditorProjectState =
        copy(timeline = timeline.moveClip(clipId, direction))

    fun withSpeed(clipId: String, speed: Float): EditorProjectState =
        copy(timeline = timeline.withSpeed(clipId, speed))

    fun withMuted(clipId: String, muted: Boolean): EditorProjectState =
        copy(timeline = timeline.withMuted(clipId, muted))

    fun withClipTransform(clipId: String, transform: GlobalTransform): EditorProjectState =
        updateClip(clipId) { it.copy(transform = transform) }

    fun withClipCrop(clipId: String, crop: GlobalCrop): EditorProjectState =
        updateClip(clipId) { it.copy(crop = crop) }

    fun withClipColor(clipId: String, color: ColorAdjustment): EditorProjectState =
        updateClip(clipId) { it.copy(color = color) }

    /**
     * Applies a single-clip mutation through the timeline model so the project's mutation
     * semantics stay identical to EditState.updateClip().
     *
     * In particular, a missing clip is a true no-op and returns the same project instance.
     */
    fun updateClip(clipId: String, update: (Clip) -> Clip): EditorProjectState {
        val updatedTimeline = timeline.updateClip(clipId, update)
        return if (updatedTimeline === timeline) this else copy(timeline = updatedTimeline)
    }

    fun withClips(clips: List<Clip>): EditorProjectState =
        copy(timeline = timeline.copy(clips = clips))
}
