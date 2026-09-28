package com.carl.editor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.carl.editor.ui.theme.CarlTheme

class MainActivity : ComponentActivity() {
    private fun persistMediaUriPermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Some picker providers grant only a transient read permission. Playback still works
            // for the current session; a future persistence layer must handle relinking.
        } catch (_: UnsupportedOperationException) {
            // Provider does not expose persistable permissions.
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var videoUri by remember { mutableStateOf<Uri?>(null) }

            val pickVideoLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                videoUri = uri
                    uri?.let { persistMediaUriPermission(it) }
            }

            CarlTheme {
                if (videoUri == null) {
                    HomeScreen(
                        onNewProjectClick = {
                            pickVideoLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                    )
                } else {
                    PreviewScreen(
                        uri = videoUri!!,
                        onBack = { videoUri = null }
                    )
                }
            }
        }
    }
}
