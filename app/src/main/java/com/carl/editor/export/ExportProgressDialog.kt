package com.carl.editor.export

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

private val ACCENT = Color(0xFF00E5A0)
private val DANGER = Color(0xFFFF5449)

/**
 * Modal overlay showing export progress/result. Renders nothing when [progress] is null.
 * Not dismissible by tapping outside while an export is actively in progress (would leave the
 * Transformer running with no visible way to check on or cancel it).
 */
@Composable
fun ExportProgressDialog(
    progress: ExportProgress?,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    if (progress == null) return

    Dialog(onDismissRequest = { if (progress !is ExportProgress.InProgress) onDismiss() }) {
        Surface(
            color = Color(0xFF121212),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (progress) {
                    is ExportProgress.InProgress -> {
                        Text("Exporting\u2026", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = progress.percent / 100f,
                            color = ACCENT,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${progress.percent}%", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onCancel) {
                            Text("Cancel", color = Color.White)
                        }
                    }
                    is ExportProgress.Success -> {
                        Text("Export complete", color = ACCENT, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Saved to app storage.",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onDismiss) {
                            Text("Done", color = ACCENT)
                        }
                    }
                    is ExportProgress.Failure -> {
                        Text("Export failed", color = DANGER, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            progress.message,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onDismiss) {
                            Text("Dismiss", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
