package com.carl.editor

/**
 * Which contextual tool panel is currently shown below the timeline. Only one panel is visible
 * at a time (see ToolDock) - this replaces the previous always-visible vertical stack of
 * Rotate/Flip + Canvas + Color panels.
 */
enum class ToolTab(val label: String) {
    TRANSFORM("Transform"),
    CANVAS("Canvas"),
    COLOR("Color")
}
