package com.carl.editor.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Test

class EditStateTest {
    private fun state(): EditState = EditState(
        clips = listOf(
            Clip(id = "a", sourceStartMs = 0L, sourceEndMs = 1_000L),
            Clip(id = "b", sourceStartMs = 1_000L, sourceEndMs = 2_000L),
            Clip(id = "c", sourceStartMs = 2_000L, sourceEndMs = 3_000L)
        )
    )

    @Test
    fun deleteRemovesOnlySelectedClip() {
        val result = state().deleteClip("b")
        assertEquals(listOf("a", "c"), result.clips.map { it.id })
    }

    @Test
    fun duplicateInsertsIndependentClipImmediatelyAfterOriginal() {
        val result = state().duplicateClip("b")
        assertEquals(4, result.clips.size)
        assertEquals(listOf("a", "b", result.clips[2].id, "c"), result.clips.map { it.id })
        assertNotEquals("b", result.clips[2].id)
        assertEquals(result.clips[1].sourceStartMs, result.clips[2].sourceStartMs)
        assertEquals(result.clips[1].sourceEndMs, result.clips[2].sourceEndMs)
    }

    @Test
    fun reorderMovesOnlyOnePositionAndPreservesIdentity() {
        val result = state().moveClip("b", 1)
        assertEquals(listOf("a", "c", "b"), result.clips.map { it.id })

        val back = result.moveClip("b", -1)
        assertEquals(listOf("a", "b", "c"), back.clips.map { it.id })
    }

    @Test
    fun invalidClipOperationsAreNoOps() {
        val original = state()
        assertSame(original, original.deleteClip("missing"))
        assertSame(original, original.duplicateClip("missing"))
        assertSame(original, original.moveClip("missing", 1))
        assertSame(original, original.moveClip("a", -1))
        assertSame(original, original.moveClip("c", 1))
    }

    @Test
    fun splitAtCutsInsideClipAndPreservesSpeed() {
        val original = EditState(
            clips = listOf(
                Clip(id = "a", sourceStartMs = 0L, sourceEndMs = 2_000L, speed = 2f)
            )
        )

        val result = original.splitAt(500L)

        assertEquals(listOf(0L, 500L), result.clips.map { it.sourceStartMs })
        assertEquals(listOf(500L, 2_000L), result.clips.map { it.sourceEndMs })
        assertEquals(listOf(2f, 2f), result.clips.map { it.speed })
        assertNotEquals(result.clips[0].id, result.clips[1].id)
        assertEquals(1_000L, result.totalDurationMs)
    }

    @Test
    fun splitAtNearClipEdgeIsNoOp() {
        val original = state()
        assertSame(original, original.splitAt(100L))
        assertSame(original, original.splitAt(900L))
    }

    @Test
    fun speedChangesTimelineDurationWithoutChangingSourceRange() {
        val original = state()
        val changed = original.withSpeed("b", 2f)
        val clip = changed.clips[1]

        assertEquals(1_000L, clip.sourceDurationMs)
        assertEquals(500L, clip.durationMs)
        assertEquals(2f, clip.speed)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonPositiveSpeedIsRejected() {
        Clip(sourceStartMs = 0L, sourceEndMs = 1_000L, speed = 0f)
    }

}
