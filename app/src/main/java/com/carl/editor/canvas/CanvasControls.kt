package com.carl.editor.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ACCENT = Color(0xFF00E5A0)

@Composable
fun CanvasControls(
    settings: CanvasSettings,
    onSelectAspectRatio: (AspectRatioPreset) -> Unit,
    onSelectBackgroundColor: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            "Canvas",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AspectRatioPreset.values().forEach { preset ->
                TextButton(onClick = { onSelectAspectRatio(preset) }) {
                    Text(
                        preset.label,
                        color = if (settings.aspectRatio == preset) ACCENT else Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(Color.Black, Color.White, Color.DarkGray).forEach { color ->
                val selected = settings.backgroundColor == color
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) ACCENT else Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onSelectBackgroundColor(color) }
                )
            }
        }
    }
}
