package com.carl.editor.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditHistoryTest {
    private fun state(ids: List<String>): EditState =
        EditState(ids.mapIndexed { index, id ->
            Clip(id = id, sourceStartMs = index * 1_000L, sourceEndMs = (index + 1) * 1_000L)
        })

    @Test
    fun undoRestoresPreviousClipOrder() {
        val original = state(listOf("a", "b", "c"))
        val edited = original.moveClip("c", -1)
        val history = EditHistory(present = original).push(edited)

        val undone = history.undo()

        assertEquals(listOf("a", "b", "c"), undone.present.clips.map { it.id })
        assertTrue(undone.canRedo)
        assertFalse(undone.canUndo)
    }

    @Test
    fun redoRestoresEditedClipOrder() {
        val original = state(listOf("a", "b", "c"))
        val edited = original.moveClip("c", -1)
        val history = EditHistory(present = original).push(edited)

        val redone = history.undo().redo()

        assertEquals(listOf("a", "c", "b"), redone.present.clips.map { it.id })
        assertFalse(redone.canRedo)
        assertTrue(redone.canUndo)
    }

    @Test
    fun newEditAfterUndoClearsRedoBranch() {
        val original = state(listOf("a", "b", "c"))
        val reordered = original.moveClip("c", -1)
        val history = EditHistory(present = original).push(reordered)
        val undone = history.undo()

        val branched = undone.push(original.deleteClip("b"))

        assertEquals(listOf("a", "c"), branched.present.clips.map { it.id })
        assertFalse(branched.canRedo)
        assertTrue(branched.canUndo)
    }

    @Test
    fun identicalStateDoesNotCreateHistoryEntry() {
        val original = state(listOf("a", "b"))
        val history = EditHistory(present = original).push(original)

        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
        assertEquals(original, history.present)
    }

    @Test
    fun syncChangesPresentWithoutCreatingUndoStep() {
        val original = state(listOf("a"))
        val synced = state(listOf("a", "b"))
        val history = EditHistory(present = original).sync(synced)

        assertEquals(synced, history.present)
        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
    }
}
