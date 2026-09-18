package com.carl.editor.timeline

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private val ACCENT = Color(0xFF00E5A0)
private val SURFACE = Color(0xFF121212)
private const val THUMBNAILS_PER_CLIP = 6

@Composable
fun TimelineControls(
    uri: Uri,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    clips: List<Clip>,
    currentClipSpeed: Float,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onSplit: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onTrimStartDragBegin: () -> Unit,
    onTrimStartDrag: (deltaMs: Long) -> Unit,
    onTrimStartDragEnd: () -> Unit,
    onTrimEndDragBegin: () -> Unit,
    onTrimEndDrag: (deltaMs: Long) -> Unit,
    onTrimEndDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var trackWidthPx by remember { mutableStateOf(1f) }

    fun pxToMs(px: Float): Long {
        if (durationMs == 0L || trackWidthPx == 0f) return 0L
        return ((px / trackWidthPx) * durationMs).toLong().coerceIn(0L, durationMs)
    }

    fun pxDeltaToMsDelta(px: Float): Long {
        if (durationMs == 0L || trackWidthPx == 0f) return 0L
        return ((px / trackWidthPx) * durationMs).toLong()
    }

    fun msToPx(ms: Long): Float {
        if (durationMs == 0L) return 0f
        return (ms.toFloat() / durationMs) * trackWidthPx
    }

    Column(modifier = modifier.fillMaxWidth().background(SURFACE).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(positionMs), color = Color.White, style = MaterialTheme.typography.bodySmall)
            Text(formatTime(durationMs), color = Color.White, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .pointerInput(durationMs) {
                    detectDragGestures { change, _ ->
                        onSeek(pxToMs(change.position.x))
                    }
                }
                .onGloballyPositioned { coords ->
                    trackWidthPx = coords.size.width.toFloat()
                }
        ) {
            // Clip filmstrip - each clip's width is proportional to its share of the timeline
            // (via Row weight, matching how durationMs already sums proportionally). Replaces the
            // old flat colored bar with real preview frames, so this reads as an actual video
            // timeline instead of a slider.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                clips.forEach { clip ->
                    ClipThumbnailStrip(
                        uri = uri,
                        clip = clip,
                        thumbnailCount = THUMBNAILS_PER_CLIP,
                        modifier = Modifier
                            .weight(clip.durationMs.toFloat().coerceAtLeast(1f))
                            .fillMaxHeight()
                    )
                }
            }

            // clip boundary markers (one per cut point between clips) - drawn on top of the
            // filmstrip so cut points stay visually clear even when adjacent clips look similar
            var elapsed = 0L
            for (clip in clips.dropLast(1)) {
                elapsed += clip.durationMs
                val boundaryPx = msToPx(elapsed)
                Box(
                    modifier = Modifier
                        .offset(x = withDp(boundaryPx - 1f))
                        .width(2.dp)
                        .height(40.dp)
                        .align(Alignment.CenterStart)
                        .background(Color.White)
                )
            }

            // playhead
            Box(
                modifier = Modifier
                    .offset(x = withDp(msToPx(positionMs) - 2f))
                    .width(4.dp)
                    .height(48.dp)
                    .align(Alignment.CenterStart)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )

            // start trim handle (always the left edge of the timeline) - drawn with a visible
            // grip mark so it reads as a draggable control, not just a colored block
            Box(
                modifier = Modifier
                    .offset(x = withDp(-8f))
                    .width(16.dp)
                    .height(48.dp)
                    .align(Alignment.CenterStart)
                    .background(ACCENT, RoundedCornerShape(4.dp))
                    .pointerInput(durationMs) {
                        detectDragGestures(
                            onDragStart = { onTrimStartDragBegin() },
                            onDragEnd = { onTrimStartDragEnd() },
                            onDragCancel = { onTrimStartDragEnd() }
                        ) { _, dragAmount ->
                            onTrimStartDrag(pxDeltaToMsDelta(dragAmount.x))
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(24.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(1.5.dp))
                )
            }

            // end trim handle (always the right edge of the timeline) - same grip treatment
            Box(
                modifier = Modifier
                    .offset(x = withDp(msToPx(durationMs) - 8f))
                    .width(16.dp)
                    .height(48.dp)
                    .align(Alignment.CenterStart)
                    .background(ACCENT, RoundedCornerShape(4.dp))
                    .pointerInput(durationMs) {
                        detectDragGestures(
                            onDragStart = { onTrimEndDragBegin() },
                            onDragEnd = { onTrimEndDragEnd() },
                            onDragCancel = { onTrimEndDragEnd() }
                        ) { _, dragAmount ->
                            onTrimEndDrag(pxDeltaToMsDelta(dragAmount.x))
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(24.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(1.5.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Undo/Redo moved to EditorTopBar (redesign Phase 2) - this row now only holds
        // Play/Pause and Split, to avoid duplicate controls for the same actions. Icon buttons
        // instead of text labels - transport controls are universally icon-recognized.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White
                )
            }
            IconButton(onClick = onSplit) {
                Icon(Icons.Filled.ContentCut, contentDescription = "Split", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Speed presets for whichever clip the playhead is currently sitting in.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(0.5f, 1f, 1.5f, 2f).forEach { speed ->
                val selected = abs(currentClipSpeed - speed) < 0.01f
                TextButton(onClick = { onSetSpeed(speed) }) {
                    Text(
                        text = formatSpeedLabel(speed),
                        color = if (selected) ACCENT else Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun formatSpeedLabel(speed: Float): String {
    val trimmed = if (speed == speed.toLong().toFloat()) speed.toLong().toString() else speed.toString()
    return "${trimmed}x"
}

@Composable
private fun withDp(px: Float): androidx.compose.ui.unit.Dp {
    val density = androidx.compose.ui.platform.LocalDensity.current
    return with(density) { px.toDp() }
}
