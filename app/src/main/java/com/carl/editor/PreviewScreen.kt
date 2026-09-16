package com.carl.editor

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.carl.editor.timeline.Clip
import com.carl.editor.timeline.EditHistory
import com.carl.editor.timeline.EditState
import com.carl.editor.timeline.TimelineControls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun PreviewScreen(uri: Uri) {
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

    val committedClips = history.present.clips
    val displayClips = draftClips ?: committedClips
    val displayDurationMs = displayClips.sumOf { it.durationMs }

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

    // Rebuild the ExoPlayer playlist whenever the committed clip list changes (split, trim commit,
    // undo, redo) - never during a live drag, which only touches draftClips.
    LaunchedEffect(committedClips) {
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
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = isPlaying
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
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
                val elapsedBefore = history.present.clipStartOnTimeline(itemIndex)
                positionMs = elapsedBefore + exoPlayer.currentPosition.coerceAtLeast(0L)
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
                exoPlayer.seekTo(index, remaining)
                return
            }
            remaining -= clip.durationMs
        }
        exoPlayer.seekTo(clips.size - 1, clips.last().durationMs)
    }

    fun commitDraft() {
        val draft = draftClips
        draftClips = null
        if (draft != null && draft != history.present.clips) {
            history = history.push(history.present.copy(clips = draft))
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer } },
                modifier = Modifier.weight(1f)
            )
            TimelineControls(
                positionMs = positionMs,
                durationMs = displayDurationMs,
                isPlaying = isPlaying,
                clips = displayClips,
                canUndo = history.canUndo,
                canRedo = history.canRedo,
                onSeek = { seekTimelineMs(it) },
                onPlayPause = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onSplit = {
                    history = history.push(history.present.splitAt(positionMs))
                },
                onUndo = { history = history.undo() },
                onRedo = { history = history.redo() },
                onTrimStartDragBegin = { draftClips = history.present.clips },
                onTrimStartDrag = { deltaMs ->
                    draftClips = draftClips?.toMutableList()?.also { list ->
                        if (list.isNotEmpty()) {
                            val first = list.first()
                            val newStart = (first.sourceStartMs + deltaMs)
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
                            val newEnd = (last.sourceEndMs + deltaMs)
                                .coerceIn(last.sourceStartMs + EditState.MIN_CLIP_MS, sourceDurationMs)
                            list[lastIndex] = last.copy(sourceEndMs = newEnd)
                        }
                    }
                },
                onTrimEndDragEnd = { commitDraft() }
            )
        }
    }
}
