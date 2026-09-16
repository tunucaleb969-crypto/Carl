package com.carl.editor.timeline

/**
 * Undo/redo stack of [EditState] snapshots.
 * [past] is oldest-first; the most recent past state is the one undo returns to.
 */
data class EditHistory(
    val past: List<EditState> = emptyList(),
    val present: EditState = EditState(),
    val future: List<EditState> = emptyList()
) {
    val canUndo: Boolean get() = past.isNotEmpty()
    val canRedo: Boolean get() = future.isNotEmpty()

    /** Records a real user edit. Clears redo history, since branching from the middle of it makes no sense. */
    fun push(newState: EditState): EditHistory {
        if (newState == present) return this
        return EditHistory(past = past + present, present = newState, future = emptyList())
    }

    /** Replaces the current state without creating an undo step (e.g. seeding duration from the source). */
    fun sync(newState: EditState): EditHistory {
        return copy(present = newState)
    }

    fun undo(): EditHistory {
        if (past.isEmpty()) return this
        return EditHistory(past = past.dropLast(1), present = past.last(), future = listOf(present) + future)
    }

    fun redo(): EditHistory {
        if (future.isEmpty()) return this
        return EditHistory(past = past + present, present = future.first(), future = future.drop(1))
    }
}
