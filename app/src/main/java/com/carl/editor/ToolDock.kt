package com.carl.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carl.editor.canvas.AspectRatioPreset
import com.carl.editor.canvas.CanvasControls
import com.carl.editor.canvas.CanvasSettings
import com.carl.editor.effects.ColorAdjustment
import com.carl.editor.effects.ColorAdjustmentControls
import com.carl.editor.effects.GlobalTransform
import com.carl.editor.effects.GlobalTransformControls

private val ACCENT = Color(0xFF00E5A0)

/**
 * Contextual tool dock: a row of tabs (Transform/Canvas/Color), with only the selected tab's
 * panel visible below it. Replaces the previous always-visible vertical stack of all three
 * panels (the direct cause of the "looks like a settings screen" complaint). Tapping the
 * already-active tab again collapses the dock (selectedTool becomes null).
 *
 * Reuses the existing GlobalTransformControls / CanvasControls / ColorAdjustmentControls
 * composables unchanged - only how/when they're shown has changed, not their internals.
 */
@Composable
fun ToolDock(
    selectedTool: ToolTab?,
    onSelectTool: (ToolTab?) -> Unit,
    globalTransform: GlobalTransform,
    onRotate: () -> Unit,
    onToggleFlipHorizontal: () -> Unit,
    onToggleFlipVertical: () -> Unit,
    canvasSettings: CanvasSettings,
    onSelectAspectRatio: (AspectRatioPreset) -> Unit,
    onSelectBackgroundColor: (Color) -> Unit,
    colorAdjustment: ColorAdjustment,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onResetColor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolTab.values().forEach { tab ->
                val selected = selectedTool == tab
                TextButton(onClick = { onSelectTool(if (selected) null else tab) }) {
                    Text(
                        tab.label,
                        color = if (selected) ACCENT else Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        when (selectedTool) {
            ToolTab.TRANSFORM -> GlobalTransformControls(
                transform = globalTransform,
                onRotate = onRotate,
                onToggleFlipHorizontal = onToggleFlipHorizontal,
                onToggleFlipVertical = onToggleFlipVertical
            )
            ToolTab.CANVAS -> CanvasControls(
                settings = canvasSettings,
                onSelectAspectRatio = onSelectAspectRatio,
                onSelectBackgroundColor = onSelectBackgroundColor
            )
            ToolTab.COLOR -> ColorAdjustmentControls(
                adjustment = colorAdjustment,
                onBrightnessChange = onBrightnessChange,
                onContrastChange = onContrastChange,
                onSaturationChange = onSaturationChange,
                onReset = onResetColor
            )
            null -> Unit
        }
    }
}
