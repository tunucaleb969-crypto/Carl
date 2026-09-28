package com.carl.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    hasSavedProject: Boolean,
    sourceNeedsRelink: Boolean,
    onOpenSavedProject: () -> Unit,
    onRelinkMedia: () -> Unit,
    onNewProjectClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when {
                    sourceNeedsRelink -> "Project media needs to be relinked"
                    hasSavedProject -> "Saved project ready"
                    else -> "No projects yet"
                },
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )

            if (sourceNeedsRelink) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your edits are still saved. Choose the original video again to continue.",
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRelinkMedia) {
                    Text("Relink Media")
                }
            } else if (hasSavedProject) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onOpenSavedProject) {
                    Text("Continue Project")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onNewProjectClick) {
                Text("New Project")
            }
        }
    }
}
