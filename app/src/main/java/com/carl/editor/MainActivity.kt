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
import com.carl.editor.persistence.CarlProjectRepository
import com.carl.editor.ui.theme.CarlTheme
import java.io.FileNotFoundException

class MainActivity : ComponentActivity() {
    private fun persistMediaUriPermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Some picker providers grant only a transient read permission.
        } catch (_: UnsupportedOperationException) {
            // Provider does not expose persistable permissions.
        }
    }

    private fun canReadMediaUri(uri: Uri): Boolean = try {
        when (uri.scheme) {
            "content" -> contentResolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
            "file" -> uri.path?.let { java.io.File(it).canRead() } == true
            else -> false
        }
    } catch (_: SecurityException) {
        false
    } catch (_: FileNotFoundException) {
        false
    } catch (_: IllegalArgumentException) {
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val projectRepository = CarlProjectRepository(this)
        val savedProject = projectRepository.load()

        setContent {
            var savedDraft by remember { mutableStateOf(savedProject) }
            var videoUri by remember {
                mutableStateOf(
                    savedProject?.sourceUri
                        ?.let(Uri::parse)
                        ?.takeIf(::canReadMediaUri)
                )
            }

            val sourceNeedsRelink = savedDraft != null && videoUri == null

            val pickVideoLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                videoUri = uri
                uri?.let { persistMediaUriPermission(it) }
            }

            val launchVideoPicker = {
                pickVideoLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            }

            CarlTheme {
                if (videoUri == null) {
                    HomeScreen(
                        hasSavedProject = savedDraft != null && !sourceNeedsRelink,
                        sourceNeedsRelink = sourceNeedsRelink,
                        onOpenSavedProject = {
                            savedDraft?.sourceUri
                                ?.let(Uri::parse)
                                ?.takeIf(::canReadMediaUri)
                                ?.let { videoUri = it }
                        },
                        onRelinkMedia = launchVideoPicker,
                        onNewProjectClick = {
                            savedDraft = null
                            projectRepository.clear()
                            launchVideoPicker()
                        }
                    )
                } else {
                    PreviewScreen(
                        uri = videoUri!!,
                        initialProjectState = savedDraft?.state ?: EditorProjectState(),
                        initialProjectName = savedDraft?.projectName ?: "Untitled Project",
                        onProjectChanged = { state ->
                            projectRepository.save(
                                savedDraft?.projectName ?: "Untitled Project",
                                videoUri!!.toString(),
                                state
                            )
                        },
                        onBack = {
                            videoUri = null
                        }
                    )
                }
            }
        }
    }
}
