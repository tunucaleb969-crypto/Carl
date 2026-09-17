package com.carl.editor.effects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ACCENT = Color(0xFF00E5A0)

/**
 * Controls for [ColorAdjustment]. Labeled "whole video" for the same reason as
 * GlobalTransformControls - this is not per-clip.
 */
@Composable
fun ColorAdjustmentControls(
    adjustment: ColorAdjustment,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sliderColors = SliderDefaults.colors(
        thumbColor = ACCENT,
        activeTrackColor = ACCENT,
        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
    )
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Color (whole video)",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall
            )
            TextButton(onClick = onReset) {
                Text("Reset", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }

        Text("Brightness", color = Color.White, style = MaterialTheme.typography.bodySmall)
        Slider(
            value = adjustment.brightness,
            onValueChange = onBrightnessChange,
            valueRange = -1f..1f,
            colors = sliderColors
        )

        Text("Contrast", color = Color.White, style = MaterialTheme.typography.bodySmall)
        Slider(
            value = adjustment.contrast,
            onValueChange = onContrastChange,
            valueRange = -1f..1f,
            colors = sliderColors
        )

        Text("Saturation", color = Color.White, style = MaterialTheme.typography.bodySmall)
        Slider(
            value = adjustment.saturation,
            onValueChange = onSaturationChange,
            valueRange = -100f..100f,
            colors = sliderColors
        )
    }
}
