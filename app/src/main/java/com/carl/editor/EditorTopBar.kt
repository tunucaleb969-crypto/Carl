package com.carl.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val ACCENT = Color(0xFF00E5A0)
private val SURFACE = Color(0xFF121212)

/**
 * The editor's top bar: back, project name, undo/redo, export.
 *
 * Icon-first controls (not text links) plus a distinct surface background separating this bar
 * from the black video canvas below it - this is the standard pattern professional mobile
 * editors use (Carl's own accent color, not a copied look) to read as an app chrome rather than
 * a plain settings list.
 *
 * Project naming/persistence doesn't exist yet, so [projectName] is a placeholder for now.
 * Export has no working implementation yet - rendered as a visibly disabled pill (communicates
 * "not yet available", never a button wired to do nothing).
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
            .background(SURFACE)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Text(
            text = projectName,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
        )

        IconButton(onClick = onUndo, enabled = canUndo) {
            Icon(
                Icons.Filled.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.35f)
            )
        }
        IconButton(onClick = onRedo, enabled = canRedo) {
            Icon(
                Icons.Filled.Redo,
                contentDescription = "Redo",
                tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.35f)
            )
        }

        // Pill-styled Export - reads as the primary/terminal action, the way professional editors
        // visually separate "finish and export" from ordinary transport controls. Intentionally
        // disabled - no export pipeline exists yet.
        TextButton(
            onClick = {},
            enabled = false,
            modifier = Modifier
                .padding(end = 4.dp)
                .background(ACCENT.copy(alpha = 0.15f), RoundedCornerShape(50))
        ) {
            Icon(
                Icons.Filled.FileUpload,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                "Export",
                color = Color.White.copy(alpha = 0.4f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
