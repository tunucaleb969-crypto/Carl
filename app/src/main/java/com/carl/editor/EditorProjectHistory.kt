package com.carl.editor

/**
 * Undo/redo history for complete editor project snapshots.
 *
 * Unlike the older timeline-only history, this history includes visual effects and canvas
 * settings, so undo/redo cannot leave the UI and export pipeline in different states.
 */
data class EditorProjectHistory(
    val past: List<EditorProjectState> = emptyList(),
    val present: EditorProjectState = EditorProjectState(),
    val future: List<EditorProjectState> = emptyList()
) {
    val canUndo: Boolean get() = past.isNotEmpty()
    val canRedo: Boolean get() = future.isNotEmpty()

    fun push(newState: EditorProjectState): EditorProjectHistory {
        if (newState == present) return this
        return copy(past = past + present, present = newState, future = emptyList())
    }

    fun sync(newState: EditorProjectState): EditorProjectHistory =
        copy(present = newState)

    fun undo(): EditorProjectHistory {
        if (past.isEmpty()) return this
        return EditorProjectHistory(
            past = past.dropLast(1),
            present = past.last(),
            future = listOf(present) + future
        )
    }

    fun redo(): EditorProjectHistory {
        if (future.isEmpty()) return this
        return EditorProjectHistory(
            past = past + present,
            present = future.first(),
            future = future.drop(1)
        )
    }
}
