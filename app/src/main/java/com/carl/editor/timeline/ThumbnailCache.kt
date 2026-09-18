package com.carl.editor.timeline

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Generates and caches small preview frames for timeline clip thumbnails.
 *
 * Deliberately conservative for performance on low-RAM devices (test target: Tecno Spark 5,
 * 2GB RAM): a fixed, modest thumbnail count per clip and small target bitmap dimensions
 * (96x96px), with a simple in-memory cache keyed by clip identity so scrolling/recomposition
 * doesn't regenerate frames that were already extracted. No disk persistence - cache is cleared
 * when the process dies, which is fine since frames are cheap to regenerate on demand.
 */
object ThumbnailCache {
    private const val THUMBNAIL_SIZE_PX = 96

    private val cache = mutableMapOf<String, List<Bitmap?>>()

    suspend fun getThumbnails(
        context: Context,
        uri: Uri,
        clip: Clip,
        count: Int
    ): List<Bitmap?> {
        val key = "$uri|${clip.sourceStartMs}|${clip.sourceEndMs}|$count"
        cache[key]?.let { return it }

        val thumbnails = withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val spanMs = (clip.sourceEndMs - clip.sourceStartMs).coerceAtLeast(1L)
                (0 until count).map { index ->
                    val denom = (count - 1).coerceAtLeast(1)
                    val fraction = if (count == 1) 0.5 else index.toDouble() / denom
                    val timeUs = (clip.sourceStartMs + (fraction * spanMs)).toLong() * 1000L
                    extractFrame(retriever, timeUs)
                }
            } finally {
                retriever.release()
            }
        }

        cache[key] = thumbnails
        return thumbnails
    }

    private fun extractFrame(retriever: MediaMetadataRetriever, timeUs: Long): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                retriever.getScaledFrameAtTime(
                    timeUs,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    THUMBNAIL_SIZE_PX,
                    THUMBNAIL_SIZE_PX
                )
            } else {
                // getScaledFrameAtTime needs API 27 (O_MR1); minSdk is 26, so this fallback path
                // must exist. Slower (extracts full-size then downscales) but only hit on
                // Android 8.0 devices specifically.
                val full = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                full?.let { Bitmap.createScaledBitmap(it, THUMBNAIL_SIZE_PX, THUMBNAIL_SIZE_PX, true) }
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Not currently called anywhere - available if memory pressure needs addressing later. */
    fun clear() {
        cache.clear()
    }
}
