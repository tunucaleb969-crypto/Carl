package com.carl.editor.effects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ACCENT = Color(0xFF00E5A0)

/**
 * Controls for [GlobalTransform]. Explicitly labeled "whole video" since this applies to every
 * clip identically - it is NOT per-clip (see GlobalTransform's kdoc for why).
 */
@Composable
fun GlobalTransformControls(
    transform: GlobalTransform,
    onRotate: () -> Unit,
    onToggleFlipHorizontal: () -> Unit,
    onToggleFlipVertical: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            "Rotate / Flip (whole video)",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onRotate) {
                Text(
                    "Rotate \u2022 ${transform.rotationDegrees.toInt()}\u00b0",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onToggleFlipHorizontal) {
                Text(
                    "Flip H",
                    color = if (transform.flipHorizontal) ACCENT else Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onToggleFlipVertical) {
                Text(
                    "Flip V",
                    color = if (transform.flipVertical) ACCENT else Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
