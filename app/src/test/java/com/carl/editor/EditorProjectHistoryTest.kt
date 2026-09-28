package com.carl.editor

import com.carl.editor.effects.ColorAdjustment
import com.carl.editor.effects.GlobalCrop
import com.carl.editor.effects.GlobalTransform
import com.carl.editor.timeline.Clip
import com.carl.editor.timeline.EditState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorProjectHistoryTest {
    private fun project(): EditorProjectState =
        EditorProjectState(
            timeline = EditState(
                listOf(Clip(id = "a", sourceStartMs = 0L, sourceEndMs = 1_000L))
            )
        )

    @Test
    fun undoRestoresVisualAndTimelineStateTogether() {
        val original = project()
        val edited = original.copy(
            timeline = original.timeline.withSpeed("a", 2f),
            globalTransform = GlobalTransform(rotationDegrees = 90f, zoom = 1.5f),
            globalCrop = GlobalCrop(0.1f, 0.2f, 0.05f, 0.15f),
            colorAdjustment = ColorAdjustment(brightness = 0.25f, contrast = -0.1f, saturation = 20f)
        )

        val history = EditorProjectHistory(present = original).push(edited)

        assertEquals(edited, history.present)
        assertEquals(original, history.undo().present)
    }

    @Test
    fun newEditAfterUndoClearsRedoBranch() {
        val original = project()
        val first = original.copy(globalTransform = GlobalTransform(rotationDegrees = 90f))
        val second = original.copy(globalCrop = GlobalCrop(0.2f))

        val history = EditorProjectHistory(present = original)
            .push(first)
            .undo()
            .push(second)

        assertEquals(second, history.present)
        assertFalse(history.canRedo)
        assertTrue(history.canUndo)
    }

    @Test
    fun syncDoesNotCreateUndoStep() {
        val original = project()
        val synced = original.copy(globalTransform = GlobalTransform(rotationDegrees = 180f))

        val history = EditorProjectHistory(present = original).sync(synced)

        assertEquals(synced, history.present)
        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
    }
}
