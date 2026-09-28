package com.carl.editor.effects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
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
    onSelectInset: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Crop (whole video)", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(0f, 0.10f, 0.20f, 0.30f).forEach { inset ->
                val selected = kotlin.math.abs(crop.normalizedInset - inset) < 0.001f
                TextButton(onClick = { onSelectInset(inset) }) {
                    Text(if (inset == 0f) "Reset" else "${(inset * 100).toInt()}%", color = if (selected) ACCENT else Color.White)
                }
            }
        }
    }
}
