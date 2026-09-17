package com.carl.editor.canvas

import androidx.compose.ui.graphics.Color

/**
 * The output frame the video is composed onto: aspect ratio (letterbox/pillarbox) and the
 * background fill color shown behind the video wherever it doesn't fill that frame.
 * Pure Compose layout - does not touch ExoPlayer video effects.
 */
data class CanvasSettings(
    val aspectRatio: AspectRatioPreset = AspectRatioPreset.ORIGINAL,
    val backgroundColor: Color = Color.Black
)

enum class AspectRatioPreset(val label: String, val ratio: Float?) {
    ORIGINAL("Original", null),
    RATIO_9_16("9:16", 9f / 16f),
    RATIO_1_1("1:1", 1f),
    RATIO_4_5("4:5", 4f / 5f),
    RATIO_16_9("16:9", 16f / 9f)
}
