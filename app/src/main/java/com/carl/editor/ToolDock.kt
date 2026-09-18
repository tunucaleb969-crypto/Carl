package com.carl.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.carl.editor.canvas.AspectRatioPreset
import com.carl.editor.canvas.CanvasControls
import com.carl.editor.canvas.CanvasSettings
import com.carl.editor.effects.ColorAdjustment
import com.carl.editor.effects.ColorAdjustmentControls
import com.carl.editor.effects.GlobalTransform
import com.carl.editor.effects.GlobalTransformControls

private val ACCENT = Color(0xFF00E5A0)
private val SURFACE = Color(0xFF121212)

private fun iconFor(tab: ToolTab): ImageVector = when (tab) {
    ToolTab.TRANSFORM -> Icons.Filled.RotateRight
    ToolTab.CANVAS -> Icons.Filled.AspectRatio
    ToolTab.COLOR -> Icons.Filled.Tune
}

/**
 * Contextual tool dock: icon+label tabs (Transform/Canvas/Color), with only the selected tab's
 * panel visible below it. Icon-over-label tabs with an accent-tinted rounded highlight on the
 * active tab is the standard bottom-dock pattern professional mobile editors use - applied here
 * with Carl's own accent color, not a copied look.
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
    Column(modifier = modifier.fillMaxWidth().background(SURFACE)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolTab.values().forEach { tab ->
                val selected = selectedTool == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelectTool(if (selected) null else tab) }
                        .background(
                            if (selected) ACCENT.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        iconFor(tab),
                        contentDescription = tab.label,
                        tint = if (selected) ACCENT else Color.White
                    )
                    Text(
                        tab.label,
                        color = if (selected) ACCENT else Color.White,
                        style = MaterialTheme.typography.labelSmall
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
