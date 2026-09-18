package com.carl.editor.timeline

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap

/**
 * Renders one clip's filmstrip: a row of evenly-spaced preview frames filling the clip's
 * allotted width. Frames come from [ThumbnailCache] (cached, so scrolling/recomposition doesn't
 * regenerate them). Slots show a dark gray placeholder until their frame loads or if extraction
 * failed for that timestamp.
 */
@Composable
fun ClipThumbnailStrip(
    uri: Uri,
    clip: Clip,
    thumbnailCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var thumbnails by remember(clip.id, clip.sourceStartMs, clip.sourceEndMs, thumbnailCount) {
        mutableStateOf<List<Bitmap?>>(emptyList())
    }

    LaunchedEffect(clip.id, clip.sourceStartMs, clip.sourceEndMs, thumbnailCount) {
        thumbnails = ThumbnailCache.getThumbnails(context, uri, clip, thumbnailCount)
    }

    Row(modifier = modifier) {
        repeat(thumbnailCount) { index ->
            val bitmap = thumbnails.getOrNull(index)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            } else {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().background(Color.DarkGray)
                )
            }
        }
    }
}
