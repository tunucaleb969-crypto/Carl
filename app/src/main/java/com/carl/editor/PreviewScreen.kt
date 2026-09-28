package com.carl.editor

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.carl.editor.canvas.CanvasSettings
import com.carl.editor.export.ExportEngine
import com.carl.editor.export.ExportProgress
import com.carl.editor.export.ExportProgressDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.carl.editor.effects.ColorAdjustment
import com.carl.editor.effects.GlobalTransform
import com.carl.editor.effects.GlobalCrop
import com.carl.editor.timeline.Clip
import com.carl.editor.timeline.EditState
import com.carl.editor.timeline.TimelineControls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// This screen calls ExoPlayer.setVideoEffects() and builds Media3 effect objects
// (ScaleAndRotateTransformation, Brightness, Contrast, HslAdjustment), all of which are
// @UnstableApi in Media3's effects framework. Kotlin enforces that as a compile error unless
// opted into - see GlobalTransform.kt / ColorAdjustment.kt for the same annotation on the
// effect-building side.
@OptIn(UnstableApi::class)
@Composable
fun PreviewScreen(
    uri: Uri,
    initialProjectState: EditorProjectState = EditorProjectState(),
    initialProjectName: String = "Untitled Project",
    onProjectChanged: (EditorProjectState, String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var projectName by remember { mutableStateOf(initialProjectName) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameDraft by remember { mutableStateOf(initialProjectName) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var history by remember { mutableStateOf(EditorProjectHistory(present = initialProjectState)) }
    var selectedClipId by remember { mutableStateOf<String?>(null) }
    var sourceDurationMs by remember { mutableLongStateOf(0L) }
    // Non-null only while a trim handle is actively being dragged; holds the live preview
    // so we don't rebuild the ExoPlayer playlist on every drag frame.
    var draftClips by remember { mutableStateOf<List<Clip>?>(null) }
    // Whole-video rotate/flip - NOT per-clip (see GlobalTransform kdoc for why).
    val globalTransform = history.present.globalTransform
    val globalCrop = history.present.globalCrop
    val colorAdjustment = history.present.colorAdjustment
    val canvasSettings = history.present.canvasSettings
    // Which contextual tool panel is shown below the timeline (redesign Phase 3) - only one at
    // a time, replacing the previous always-visible vertical stack of all three panels.
    var selectedTool by remember { mutableStateOf<ToolTab?>(null) }
    // Surfaced when ExoPlayer reports a playback error, so failures are visible instead of
    // silent (a blank/frozen preview with no explanation is exactly what we want to avoid).
    var playerErrorMessage by remember { mutableStateOf<String?>(null) }
    var exportProgress by remember { mutableStateOf<ExportProgress?>(null) }
    var exportJob by remember { mutableStateOf<Job?>(null) }
    val exportEngine = remember(context) { ExportEngine(context) }
    val exportScope = rememberCoroutineScope()

    // Debounced autosave: editor gestures can update state many times per second, so persistence
    // waits briefly for the current burst to settle instead of writing on every slider frame.
    LaunchedEffect(history.present) {
        delay(350)
        onProjectChanged(history.present, projectName)
    }

    val committedClips = history.present.clips
    val displayClips = draftClips ?: committedClips
    val displayDurationMs = displayClips.sumOf { it.durationMs }

    fun applySpeedForCurrentItem() {
        val clips = history.present.clips
        val itemIndex = exoPlayer.currentMediaItemIndex
        if (itemIndex in clips.indices) {
            exoPlayer.playbackParameters = PlaybackParameters(clips[itemIndex].speed)
        }
    }

    // Learn the source's real duration via MediaMetadataRetriever (cheap - no decoder spun up),
    // then seed a single full-length clip covering the whole video.
    LaunchedEffect(uri) {
        if (sourceDurationMs == 0L) {
            val duration = withContext(Dispatchers.IO) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull() ?: 0L
                } finally {
                    retriever.release()
                }
            }
            sourceDurationMs = duration
            val reconciledState = history.present.reconcileSourceDuration(duration)
            history = history.sync(reconciledState)
            if (selectedClipId == null) {
                selectedClipId = reconciledState.clips.firstOrNull()?.id
            }
        }
    }

    LaunchedEffect(committedClips) {
        if (selectedClipId != null && committedClips.none { it.id == selectedClipId }) {
            selectedClipId = committedClips.firstOrNull()?.id
        }
    }

    LaunchedEffect(positionMs, committedClips) {
        val index = history.present.clipIndexAt(positionMs)
        val clipId = history.present.clips.getOrNull(index)?.id
        if (clipId != null && clipId != selectedClipId) {
            selectedClipId = clipId
        }
    }

    fun seekTimelineMs(target: Long) {
        val clips = history.present.clips
        if (clips.isEmpty()) return
        var remaining = target.coerceIn(0L, clips.sumOf { it.durationMs })
        for ((index, clip) in clips.withIndex()) {
            if (remaining <= clip.durationMs) {
                val sourcePositionMs = (remaining * clip.speed).toLong()
                exoPlayer.seekTo(index, sourcePositionMs)
                applySpeedForCurrentItem()
                return
            }
            remaining -= clip.durationMs
        }
        val lastIndex = clips.size - 1
        exoPlayer.seekTo(lastIndex, clips.last().sourceDurationMs)
        applySpeedForCurrentItem()
    }

    // Rebuild the ExoPlayer playlist whenever the committed clip list OR either whole-video effect
    // set changes (split, trim commit, undo, redo, speed change, rotate, flip, color adjustment) -
    // never during a live drag, which only touches draftClips. setVideoEffects() must be called
    // before prepare(), and dynamically swapping effects on an already-prepared player has known
    // stability issues, so we always go through this same rebuild path rather than hot-swapping
    // effects in place.
    LaunchedEffect(committedClips, globalTransform, globalCrop, colorAdjustment) {
        if (committedClips.isEmpty()) return@LaunchedEffect
        val mediaItems = committedClips.map { clip ->
            MediaItem.Builder()
                .setUri(uri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(clip.sourceStartMs)
                        .setEndPositionMs(clip.sourceEndMs)
                        .build()
                )
                .build()
        }
        // Rebuilds are required when timeline/effect state changes, but they must not make
        // editing feel like a reset. Preserve the user's timeline position across the rebuild,
        // clamping naturally if the edit shortened the project.
        val preservedTimelinePositionMs = positionMs
        exoPlayer.setVideoEffects(globalCrop.toEffects() + globalTransform.toEffects() + colorAdjustment.toEffects())
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        seekTimelineMs(preservedTimelinePositionMs)
        exoPlayer.playWhenReady = isPlaying
        applySpeedForCurrentItem()
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                // Successfully playing again clears any previous error banner.
                if (playing) playerErrorMessage = null
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Each clip may have its own speed - re-apply as playback crosses into the next one.
                applySpeedForCurrentItem()
            }
            override fun onPlayerError(error: PlaybackException) {
                // Never fail silently: codec issues, corrupted files, or an effects-pipeline
                // failure should be visible to the user, not just a blank/frozen preview.
                playerErrorMessage = error.errorCodeName.replace('_', ' ').lowercase()
                    .replaceFirstChar { it.uppercase() }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            val clips = history.present.clips
            val itemIndex = exoPlayer.currentMediaItemIndex
            if (itemIndex in clips.indices) {
                val clip = clips[itemIndex]
                val elapsedBefore = history.present.clipStartOnTimeline(itemIndex)
                val sourcePositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                // currentPosition tracks source-space playback progress regardless of speed;
                // convert back to timeline time by dividing out the clip's speed.
                positionMs = elapsedBefore + (sourcePositionMs / clip.speed).toLong()
            }
            delay(200)
        }
    }

    fun commitDraft() {
        val draft = draftClips
        draftClips = null
        if (draft != null && draft != history.present.clips) {
            history = history.push(history.present.withClips(draft))
        }
    }

    val currentClipSpeed = history.present.clipIndexAt(positionMs)
        .takeIf { it >= 0 }
        ?.let { history.present.clips[it].speed }
        ?: 1f

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        color = Color.Black
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Project identity and autosave are provided by the persistence layer.
            EditorTopBar(
                projectName = projectName,
                canUndo = history.canUndo,
                canRedo = history.canRedo,
                canExport = committedClips.isNotEmpty() && exportJob?.isActive != true,
                onBack = onBack,
                onUndo = { history = history.undo() },
                onRedo = { history = history.redo() },
                onRename = {
                    renameDraft = projectName
                    showRenameDialog = true
                },
                onExport = {
                    exportJob?.cancel()
                    exportProgress = ExportProgress.InProgress(0)
                    exportJob = exportScope.launch {
                        exportEngine.export(
                            uri,
                            committedClips,
                            globalCrop.toEffects() + globalTransform.toEffects() + colorAdjustment.toEffects()
                        ).collect { progress ->
                            exportProgress = progress
                        }
                    }
                }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(canvasSettings.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                val ratio = canvasSettings.aspectRatio.ratio
                val playerModifier = if (ratio != null) {
                    Modifier.fillMaxHeight().aspectRatio(ratio, matchHeightConstraintsFirst = true)
                } else {
                    Modifier.fillMaxSize()
                }
                AndroidView(
                    factory = { ctx ->
                        // Inflate from XML rather than constructing PlayerView(ctx) directly: this
                        // is the only way to set surface_type=texture_view, which is required to
                        // avoid a well-documented Media3 bug where setVideoEffects() produces a
                        // black preview with the default SurfaceView (see
                        // R.layout.player_view_texture for the full explanation).
                        val playerView = LayoutInflater.from(ctx)
                            .inflate(R.layout.player_view_texture, null) as PlayerView
                        playerView.apply {
                            player = exoPlayer
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            // We already have a full custom transport (play/pause, seek, split, trim)
                            // in TimelineControls below - PlayerView's own default overlay (rewind/
                            // forward/prev/next buttons) is redundant and looks like a generic media
                            // player, not an editor. Disable it (also set in the XML, kept here too
                            // for clarity/defensiveness).
                            useController = false
                        }
                    },
                    modifier = playerModifier
                )
                // Visible error banner instead of a silent blank/frozen preview.
                playerErrorMessage?.let { message ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "Playback error: $message",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            TimelineControls(
                uri = uri,
                positionMs = positionMs,
                durationMs = displayDurationMs,
                isPlaying = isPlaying,
                clips = displayClips,
                selectedClipId = selectedClipId,
                currentClipSpeed = currentClipSpeed,
                onSeek = { seekTimelineMs(it) },
                onSelectClip = { id ->
                    selectedClipId = id
                    val index = history.present.indexOfClip(id)
                    if (index >= 0) {
                        seekTimelineMs(history.present.clipStartOnTimeline(index))
                    }
                },
                onDeleteClip = {
                    val id = selectedClipId
                    if (id != null) {
                        val before = history.present
                        val currentIndex = before.indexOfClip(id)
                        if (currentIndex >= 0) {
                            val newState = before.deleteClip(id)
                            history = history.push(newState)
                            val replacementIndex = currentIndex.coerceAtMost(newState.clips.lastIndex)
                            selectedClipId = newState.clips.getOrNull(replacementIndex)?.id
                                ?: newState.clips.lastOrNull()?.id
                            if (replacementIndex >= 0) {
                                seekTimelineMs(newState.clipStartOnTimeline(replacementIndex))
                            } else {
                                seekTimelineMs(0L)
                            }
                        }
                    }
                },
                onDuplicateClip = {
                    val id = selectedClipId
                    if (id != null) {
                        val before = history.present
                        val originalIndex = before.indexOfClip(id)
                        if (originalIndex >= 0) {
                            val newState = before.duplicateClip(id)
                            history = history.push(newState)
                            val duplicateIndex = (originalIndex + 1).coerceAtMost(newState.clips.lastIndex)
                            selectedClipId = newState.clips.getOrNull(duplicateIndex)?.id ?: id
                            if (duplicateIndex >= 0) {
                                seekTimelineMs(newState.clipStartOnTimeline(duplicateIndex))
                            }
                        }
                    }
                },
                onMoveClipUp = {
                    val id = selectedClipId
                    if (id != null) {
                        val before = history.present
                        val oldIndex = before.indexOfClip(id)
                        if (oldIndex >= 0) {
                            val oldStart = before.clipStartOnTimeline(oldIndex)
                            val oldOffset = (positionMs - oldStart)
                                .coerceIn(0L, before.clips[oldIndex].durationMs)
                            val updated = before.moveClip(id, -1)
                            if (updated !== before) {
                                history = history.push(updated)
                                selectedClipId = id
                                val newIndex = updated.indexOfClip(id)
                                seekTimelineMs(updated.clipStartOnTimeline(newIndex) + oldOffset)
                            }
                        }
                    }
                },
                onMoveClipDown = {
                    val id = selectedClipId
                    if (id != null) {
                        val before = history.present
                        val oldIndex = before.indexOfClip(id)
                        if (oldIndex >= 0) {
                            val oldStart = before.clipStartOnTimeline(oldIndex)
                            val oldOffset = (positionMs - oldStart)
                                .coerceIn(0L, before.clips[oldIndex].durationMs)
                            val updated = before.moveClip(id, 1)
                            if (updated !== before) {
                                history = history.push(updated)
                                selectedClipId = id
                                val newIndex = updated.indexOfClip(id)
                                seekTimelineMs(updated.clipStartOnTimeline(newIndex) + oldOffset)
                            }
                        }
                    }
                },
                onPlayPause = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onSplit = {
                    history = history.push(history.present.splitAt(positionMs))
                },
                onSetSpeed = { speed ->
                    val index = history.present.clipIndexAt(positionMs)
                    if (index >= 0) {
                        val clipId = history.present.clips[index].id
                        history = history.push(history.present.withSpeed(clipId, speed))
                    }
                },
                onTrimStartDragBegin = { draftClips = history.present.clips },
                onTrimStartDrag = { deltaMs ->
                    draftClips = draftClips?.toMutableList()?.also { list ->
                        if (list.isNotEmpty()) {
                            val first = list.first()
                            // deltaMs is timeline-space; convert to source-space via this clip's speed.
                            val sourceDelta = (deltaMs * first.speed).toLong()
                            val newStart = (first.sourceStartMs + sourceDelta)
                                .coerceIn(0L, first.sourceEndMs - EditState.MIN_CLIP_MS)
                            list[0] = first.copy(sourceStartMs = newStart)
                        }
                    }
                },
                onTrimStartDragEnd = { commitDraft() },
                onTrimEndDragBegin = { draftClips = history.present.clips },
                onTrimEndDrag = { deltaMs ->
                    draftClips = draftClips?.toMutableList()?.also { list ->
                        if (list.isNotEmpty()) {
                            val lastIndex = list.size - 1
                            val last = list[lastIndex]
                            val sourceDelta = (deltaMs * last.speed).toLong()
                            val newEnd = (last.sourceEndMs + sourceDelta)
                                .coerceIn(last.sourceStartMs + EditState.MIN_CLIP_MS, sourceDurationMs)
                            list[lastIndex] = last.copy(sourceEndMs = newEnd)
                        }
                    }
                },
                onTrimEndDragEnd = { commitDraft() }
            )
            ToolDock(
                selectedTool = selectedTool,
                onSelectTool = { selectedTool = it },
                globalTransform = globalTransform,
                globalCrop = globalCrop,
                onSelectCropInsets = { left, right, top, bottom -> history = history.push(history.present.copy(globalCrop = GlobalCrop(left, right, top, bottom))) },
                onRotate = { history = history.push(history.present.copy(globalTransform = globalTransform.rotatedClockwise())) },
                onToggleFlipHorizontal = {
                    history = history.push(history.present.copy(globalTransform = globalTransform.copy(flipHorizontal = !globalTransform.flipHorizontal)))
                },
                onToggleFlipVertical = {
                    history = history.push(history.present.copy(globalTransform = globalTransform.copy(flipVertical = !globalTransform.flipVertical)))
                },
                onZoomOut = { history = history.push(history.present.copy(globalTransform = globalTransform.zoomedBy(-0.25f))) },
                onZoomIn = { history = history.push(history.present.copy(globalTransform = globalTransform.zoomedBy(0.25f))) },
                onPan = { dx, dy -> history = history.push(history.present.copy(globalTransform = globalTransform.pannedBy(dx, dy))) },
                onResetFraming = { history = history.push(history.present.copy(globalTransform = globalTransform.resetFraming())) },
                canvasSettings = canvasSettings,
                onSelectAspectRatio = { preset -> history = history.push(history.present.copy(canvasSettings = canvasSettings.copy(aspectRatio = preset))) },
                onSelectBackgroundColor = { color -> history = history.push(history.present.copy(canvasSettings = canvasSettings.copy(backgroundColor = color))) },
                colorAdjustment = colorAdjustment,
                onBrightnessChange = { value -> history = history.push(history.present.copy(colorAdjustment = colorAdjustment.copy(brightness = value))) },
                onContrastChange = { value -> history = history.push(history.present.copy(colorAdjustment = colorAdjustment.copy(contrast = value))) },
                onSaturationChange = { value -> history = history.push(history.present.copy(colorAdjustment = colorAdjustment.copy(saturation = value))) },
                onResetColor = { history = history.push(history.present.copy(colorAdjustment = ColorAdjustment())) }
            )
        }

        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename project") },
                text = {
                    OutlinedTextField(
                        value = renameDraft,
                        onValueChange = { renameDraft = it.take(80) },
                        singleLine = true,
                        label = { Text("Project name") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val normalized = renameDraft.trim().ifBlank { "Untitled Project" }
                            projectName = normalized
                            showRenameDialog = false
                            onProjectChanged(history.present, normalized)
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        ExportProgressDialog(
            progress = exportProgress,
            onDismiss = {
                exportProgress = null
                exportJob = null
            },
            onCancel = {
                exportJob?.cancel()
                exportJob = null
                exportProgress = null
            },
            onOpen = { uri ->
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, uri).apply {
                            setDataAndType(uri, "video/mp4")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    )
                }.onFailure { error ->
                    if (error is ActivityNotFoundException) Unit
                }
            },
            onShare = { uri ->
                runCatching {
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "video/mp4"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            },
                            "Share Carl export"
                        )
                    )
                }.onFailure { error ->
                    if (error is ActivityNotFoundException) Unit
                }
            }
        )
    }
}
