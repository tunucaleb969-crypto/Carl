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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ACCENT = Color(0xFF00E5A0)

@Composable
fun GlobalCropControls(
    crop: GlobalCrop,
    onSelectCropInsets: (Float, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Crop (whole video)", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        CropSlider("Left", crop.normalizedLeft) { onSelectCropInsets(it, crop.normalizedRight, crop.normalizedTop, crop.normalizedBottom) }
        CropSlider("Right", crop.normalizedRight) { onSelectCropInsets(crop.normalizedLeft, it, crop.normalizedTop, crop.normalizedBottom) }
        CropSlider("Top", crop.normalizedTop) { onSelectCropInsets(crop.normalizedLeft, crop.normalizedRight, it, crop.normalizedBottom) }
        CropSlider("Bottom", crop.normalizedBottom) { onSelectCropInsets(crop.normalizedLeft, crop.normalizedRight, crop.normalizedTop, it) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { onSelectCropInsets(0f, 0f, 0f, 0f) }) {
                Text("Reset", color = if (crop.normalizedInset == 0f) ACCENT else Color.White)
            }
            listOf(0.10f, 0.20f, 0.30f).forEach { inset ->
                TextButton(onClick = { onSelectCropInsets(inset, inset, inset, inset) }) {
                    Text("${(inset * 100).toInt()}%", color = if (kotlin.math.abs(crop.normalizedInset - inset) < 0.001f) ACCENT else Color.White)
                }
            }
        }
    }
}

@Composable
private fun CropSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("$label ${(value * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.labelSmall)
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..0.45f)
    }
}