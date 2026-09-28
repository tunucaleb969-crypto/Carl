package com.carl.editor

import com.carl.editor.timeline.Clip
import com.carl.editor.timeline.EditState
import com.carl.editor.effects.ColorAdjustment
import com.carl.editor.effects.GlobalCrop
import com.carl.editor.effects.GlobalTransform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class EditorProjectStateTest {
    private fun state(): EditorProjectState = EditorProjectState(
        timeline = EditState(
            clips = listOf(
                Clip(id = "a", sourceStartMs = 0L, sourceEndMs = 1_000L),
                Clip(id = "b", sourceStartMs = 1_000L, sourceEndMs = 2_000L),
                Clip(id = "c", sourceStartMs = 2_000L, sourceEndMs = 3_000L)
            )
        )
    )

    @Test
    fun updateClipChangesOnlyTargetAndPreservesProjectVisualState() {
        val original = state().copy(
            globalTransform = GlobalTransform(rotationDegrees = 90f, zoom = 1.5f),
            globalCrop = GlobalCrop(leftInset = 0.1f),
            colorAdjustment = ColorAdjustment(brightness = 0.2f)
        )

        val updated = original.updateClip("b") { it.copy(speed = 2f) }

        assertEquals(listOf("a", "b", "c"), updated.clips.map { it.id })
        assertEquals(1f, updated.clips[0].speed)
        assertEquals(2f, updated.clips[1].speed)
        assertEquals(1f, updated.clips[2].speed)
        assertEquals(original.globalTransform, updated.globalTransform)
        assertEquals(original.globalCrop, updated.globalCrop)
        assertEquals(original.colorAdjustment, updated.colorAdjustment)
    }

    @Test
    fun clipVisualMutationsStayScopedToSelectedClip() {
        val original = state()
        val updated = original
            .withClipTransform("b", GlobalTransform(rotationDegrees = 90f))
            .withClipCrop("b", GlobalCrop(leftInset = 0.1f))
            .withClipColor("b", ColorAdjustment(brightness = 0.2f))

        assertEquals(0f, updated.clips[0].transform.rotationDegrees)
        assertEquals(90f, updated.clips[1].transform.rotationDegrees)
        assertEquals(0f, updated.clips[2].transform.rotationDegrees)
        assertEquals(0f, updated.clips[0].crop.leftInset)
        assertEquals(0.1f, updated.clips[1].crop.leftInset)
        assertEquals(0f, updated.clips[2].crop.leftInset)
        assertEquals(0f, updated.clips[0].color.brightness)
        assertEquals(0.2f, updated.clips[1].color.brightness)
        assertEquals(0f, updated.clips[2].color.brightness)
    }

    @Test
    fun updateMissingClipIsTrueProjectNoOp() {
        val original = state()
        val updated = original.updateClip("missing") { it.copy(speed = 2f) }

        assertSame(original, updated)
    }
}
