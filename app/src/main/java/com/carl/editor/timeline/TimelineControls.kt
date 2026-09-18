package com.carl.editor.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private val ACCENT = Color(0xFF00E5A0)

@Composable
fun TimelineControls(
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

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
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
                .height(48.dp)
                .pointerInput(durationMs) {
                    detectDragGestures { change, _ ->
                        onSeek(pxToMs(change.position.x))
                    }
                }
                .onGloballyPositioned { coords ->
                    trackWidthPx = coords.size.width.toFloat()
                }
        ) {
            // background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .align(Alignment.CenterStart)
                    .background(Color.DarkGray, RoundedCornerShape(3.dp))
            )

            // clip boundary markers (one per cut point between clips)
            var elapsed = 0L
            for (clip in clips.dropLast(1)) {
                elapsed += clip.durationMs
                val boundaryPx = msToPx(elapsed)
                Box(
                    modifier = Modifier
                        .offset(x = withDp(boundaryPx - 1f))
                        .width(2.dp)
                        .height(24.dp)
                        .align(Alignment.CenterStart)
                        .background(Color.White)
                )
            }

            // active range fill (always the full current duration, since trimming is edge-based now)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .align(Alignment.CenterStart)
                    .background(ACCENT, RoundedCornerShape(3.dp))
            )

            // playhead
            Box(
                modifier = Modifier
                    .offset(x = withDp(msToPx(positionMs) - 2f))
                    .width(4.dp)
                    .height(32.dp)
                    .align(Alignment.CenterStart)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )

            // start trim handle (always the left edge of the timeline) - drawn with a visible
            // grip mark so it reads as a draggable control, not just a colored block
            Box(
                modifier = Modifier
                    .offset(x = withDp(-8f))
                    .width(16.dp)
                    .height(40.dp)
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
                        .height(20.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(1.5.dp))
                )
            }

            // end trim handle (always the right edge of the timeline) - same grip treatment
            Box(
                modifier = Modifier
                    .offset(x = withDp(msToPx(durationMs) - 8f))
                    .width(16.dp)
                    .height(40.dp)
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
                        .height(20.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(1.5.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Undo/Redo moved to EditorTopBar (redesign Phase 2) - this row now only holds
        // Play/Pause and Split, to avoid duplicate controls for the same actions.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onPlayPause) {
                Text(
                    if (isPlaying) "Pause" else "Play",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            TextButton(onClick = onSplit) {
                Text("Split", color = Color.White, style = MaterialTheme.typography.labelLarge)
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
