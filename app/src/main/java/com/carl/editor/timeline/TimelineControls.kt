package com.carl.editor.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

@Composable
fun TimelineControls(
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    trimState: TrimState,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onTrimChange: (TrimState) -> Unit,
    modifier: Modifier = Modifier
) {
    var trackWidthPx by remember { mutableStateOf(1f) }

    fun pxToMs(px: Float): Long {
        if (durationMs == 0L || trackWidthPx == 0f) return 0L
        return ((px / trackWidthPx) * durationMs).toLong().coerceIn(0L, durationMs)
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
            Text(formatTime(positionMs), style = MaterialTheme.typography.bodySmall)
            Text(formatTime(durationMs), style = MaterialTheme.typography.bodySmall)
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

            // trimmed (active) range highlight
            val startPx = msToPx(trimState.startMs)
            val endPx = msToPx(trimState.endMs)
            Box(
                modifier = Modifier
                    .offset(x = withDp(startPx))
                    .width(withDp((endPx - startPx).coerceAtLeast(0f)))
                    .height(6.dp)
                    .align(Alignment.CenterStart)
                    .background(Color(0xFF00E5A0), RoundedCornerShape(3.dp))
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

            // start trim handle
            Box(
                modifier = Modifier
                    .offset(x = withDp(startPx - 8f))
                    .width(16.dp)
                    .height(40.dp)
                    .align(Alignment.CenterStart)
                    .background(Color(0xFF00E5A0), RoundedCornerShape(4.dp))
                    .pointerInput(durationMs) {
                        detectDragGestures { change, _ ->
                            onTrimChange(trimState.setStart(pxToMs(change.position.x + startPx)))
                        }
                    }
            )

            // end trim handle
            Box(
                modifier = Modifier
                    .offset(x = withDp(endPx - 8f))
                    .width(16.dp)
                    .height(40.dp)
                    .align(Alignment.CenterStart)
                    .background(Color(0xFF00E5A0), RoundedCornerShape(4.dp))
                    .pointerInput(durationMs) {
                        detectDragGestures { change, _ ->
                            onTrimChange(trimState.setEnd(pxToMs(change.position.x + endPx)))
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onPlayPause) {
                Text(if (isPlaying) "⏸" else "▶", style = MaterialTheme.typography.titleLarge)
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

@Composable
private fun withDp(px: Float): androidx.compose.ui.unit.Dp {
    val density = androidx.compose.ui.platform.LocalDensity.current
    return with(density) { px.toDp() }
}
