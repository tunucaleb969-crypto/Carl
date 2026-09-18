package com.carl.editor

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.carl.editor.canvas.CanvasSettings
import com.carl.editor.effects.ColorAdjustment
import com.carl.editor.effects.GlobalTransform
import com.carl.editor.timeline.Clip
import com.carl.editor.timeline.EditHistory
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
fun PreviewScreen(uri: Uri, onBack: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var history by remember { mutableStateOf(EditHistory()) }
    var sourceDurationMs by remember { mutableLongStateOf(0L) }
    // Non-null only while a trim handle is actively being dragged; holds the live preview
    // so we don't rebuild the ExoPlayer playlist on every drag frame.
    var draftClips by remember { mutableStateOf<List<Clip>?>(null) }
    // Whole-video rotate/flip - NOT per-clip (see GlobalTransform kdoc for why).
    var globalTransform by remember { mutableStateOf(GlobalTransform()) }
    // Whole-video brightness/contrast/saturation - same scope limitation as globalTransform.
    var colorAdjustment by remember { mutableStateOf(ColorAdjustment()) }
    // Output frame: aspect ratio + background fill. Pure Compose layout, independent of
    // globalTransform / colorAdjustment / ExoPlayer video effects.
    var canvasSettings by remember { mutableStateOf(CanvasSettings()) }
    // Which contextual tool panel is shown below the timeline (redesign Phase 3) - only one at
    // a time, replacing the previous always-visible vertical stack of all three panels.
    var selectedTool by remember { mutableStateOf<ToolTab?>(null) }

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
            history = history.sync(history.present.withSourceDuration(duration))
        }
    }

    // Rebuild the ExoPlayer playlist whenever the committed clip list OR either whole-video effect
    // set changes (split, trim commit, undo, redo, speed change, rotate, flip, color adjustment) -
    // never during a live drag, which only touches draftClips. setVideoEffects() must be called
    // before prepare(), and dynamically swapping effects on an already-prepared player has known
    // stability issues, so we always go through this same rebuild path rather than hot-swapping
    // effects in place.
    LaunchedEffect(committedClips, globalTransform, colorAdjustment) {
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
        exoPlayer.setVideoEffects(globalTransform.toEffects() + colorAdjustment.toEffects())
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = isPlaying
        applySpeedForCurrentItem()
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Each clip may have its own speed - re-apply as playback crosses into the next one.
                applySpeedForCurrentItem()
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

    fun commitDraft() {
        val draft = draftClips
        draftClips = null
        if (draft != null && draft != history.present.clips) {
            history = history.push(history.present.copy(clips = draft))
        }
    }

    val currentClipSpeed = history.present.clipIndexAt(positionMs)
        .takeIf { it >= 0 }
        ?.let { history.present.clips[it].speed }
        ?: 1f

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Project naming/persistence doesn't exist yet - placeholder name for now.
            EditorTopBar(
                projectName = "Untitled Project",
                canUndo = history.canUndo,
                canRedo = history.canRedo,
                onBack = onBack,
                onUndo = { history = history.undo() },
                onRedo = { history = history.redo() }
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
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            // We already have a full custom transport (play/pause, seek, split, trim)
                            // in TimelineControls below - PlayerView's own default overlay (rewind/
                            // forward/prev/next buttons) is redundant and looks like a generic media
                            // player, not an editor. Disable it.
                            useController = false
                        }
                    },
                    modifier = playerModifier
                )
            }
            TimelineControls(
                uri = uri,
                positionMs = positionMs,
                durationMs = displayDurationMs,
                isPlaying = isPlaying,
                clips = displayClips,
                currentClipSpeed = currentClipSpeed,
                onSeek = { seekTimelineMs(it) },
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
                onRotate = { globalTransform = globalTransform.rotatedClockwise() },
                onToggleFlipHorizontal = {
                    globalTransform = globalTransform.copy(flipHorizontal = !globalTransform.flipHorizontal)
                },
                onToggleFlipVertical = {
                    globalTransform = globalTransform.copy(flipVertical = !globalTransform.flipVertical)
                },
                canvasSettings = canvasSettings,
                onSelectAspectRatio = { preset -> canvasSettings = canvasSettings.copy(aspectRatio = preset) },
                onSelectBackgroundColor = { color -> canvasSettings = canvasSettings.copy(backgroundColor = color) },
                colorAdjustment = colorAdjustment,
                onBrightnessChange = { colorAdjustment = colorAdjustment.copy(brightness = it) },
                onContrastChange = { colorAdjustment = colorAdjustment.copy(contrast = it) },
                onSaturationChange = { colorAdjustment = colorAdjustment.copy(saturation = it) },
                onResetColor = { colorAdjustment = ColorAdjustment() }
            )
        }
    }
}
