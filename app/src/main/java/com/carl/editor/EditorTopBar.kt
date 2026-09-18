package com.carl.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * The editor's top bar: back, project name, undo/redo, export.
 *
 * Project naming/persistence doesn't exist yet (see PROJECT_STATE.md, later redesign phase), so
 * [projectName] is a placeholder string for now, not a real editable/persisted value.
 *
 * Export has no working implementation yet (no Transformer/export pipeline built). Per this
 * project's "no fake buttons" rule, the Export button is rendered visibly but disabled/dimmed
 * rather than wired to a callback that would do nothing - it communicates "not yet available",
 * not "broken".
 */
@Composable
fun EditorTopBar(
    projectName: String,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onBack) {
            Text("Back", color = Color.White, style = MaterialTheme.typography.labelLarge)
        }

        Text(
            text = projectName,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
        )

        TextButton(onClick = onUndo, enabled = canUndo) {
            Text(
                "Undo",
                color = if (canUndo) Color.White else Color.White.copy(alpha = 0.35f),
                style = MaterialTheme.typography.labelLarge
            )
        }
        TextButton(onClick = onRedo, enabled = canRedo) {
            Text(
                "Redo",
                color = if (canRedo) Color.White else Color.White.copy(alpha = 0.35f),
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Intentionally disabled - no export pipeline exists yet. Visible so the eventual feature
        // is discoverable, but never clickable until it actually does something.
        TextButton(onClick = {}, enabled = false) {
            Text(
                "Export",
                color = Color.White.copy(alpha = 0.35f),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
