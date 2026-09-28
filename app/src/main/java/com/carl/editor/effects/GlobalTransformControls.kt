package com.carl.editor.effects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ACCENT = Color(0xFF00E5A0)

@Composable
fun GlobalTransformControls(
    transform: GlobalTransform,
    onRotate: () -> Unit,
    onToggleFlipHorizontal: () -> Unit,
    onToggleFlipVertical: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    onPan: (Float, Float) -> Unit,
    onResetFraming: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            "Transform (whole video)",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onRotate) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = Color.White)
                Text(" ${transform.rotationDegrees.toInt()}°", color = Color.White)
            }
            TextButton(onClick = onToggleFlipHorizontal) {
                Icon(Icons.Filled.Flip, contentDescription = "Flip horizontal", tint = if (transform.flipHorizontal) ACCENT else Color.White)
                Text(" H", color = if (transform.flipHorizontal) ACCENT else Color.White)
            }
            TextButton(onClick = onToggleFlipVertical) {
                Icon(Icons.Filled.Flip, contentDescription = "Flip vertical", tint = if (transform.flipVertical) ACCENT else Color.White)
                Text(" V", color = if (transform.flipVertical) ACCENT else Color.White)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onZoomOut) { Text("−", color = Color.White) }
            Text(
                "Zoom ${(transform.normalizedZoom * 100).toInt()}%",
                color = ACCENT,
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onZoomIn) { Text("+", color = Color.White) }
            TextButton(onClick = onResetFraming) { Text("Reset", color = Color.White) }
        }
        Text(
            "Pan",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 2.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { onPan(-0.1f, 0f) }) { Text("←", color = Color.White) }
            TextButton(onClick = { onPan(0f, -0.1f) }) {
                Icon(Icons.Filled.ArrowUpward, contentDescription = "Pan up", tint = Color.White)
            }
            TextButton(onClick = { onPan(0.1f, 0f) }) { Text("→", color = Color.White) }
            TextButton(onClick = { onPan(0f, 0.1f) }) {
                Icon(Icons.Filled.ArrowDownward, contentDescription = "Pan down", tint = Color.White)
            }
        }
    }
}
