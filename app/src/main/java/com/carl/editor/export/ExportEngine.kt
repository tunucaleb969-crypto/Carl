package com.carl.editor.export

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.carl.editor.timeline.Clip
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

sealed class ExportProgress {
    data class InProgress(val percent: Int) : ExportProgress()
    data class Success(val outputUri: Uri) : ExportProgress()
    data class Failure(val message: String) : ExportProgress()
}

/**
 * Exports the current clip list to a single MP4 file via Media3 Transformer - the real,
 * official Media3 export API (androidx.media3:media3-transformer), not a placeholder.
 *
 * VERIFICATION STATUS: logically validated against Media3's documented Transformer/Composition
 * API shape; NOT yet run on a device. Transformer's progress-polling and listener-threading
 * behavior in particular should be treated as unconfirmed until a real export has been tested.
 *
 * SCOPE FOR THIS FIRST VERSION (deliberate, not an oversight):
 * - Concatenates [Clip]s in order, honoring each clip's trimmed in/out points via the same
 *   MediaItem.ClippingConfiguration approach already used for preview.
 * - Does NOT bake in per-clip speed, rotate/flip, or color adjustments yet. Speed specifically
 *   needs a SpeedChangeEffect with real timestamp remapping for a correct exported file -
 *   reusing ExoPlayer.playbackParameters (preview-only, live playback rate) would silently
 *   produce a normal-speed file while claiming to honor the speed setting. Rather than ship
 *   that incorrect behavior, it's deferred to a follow-up step.
 * - Output format: Transformer's own defaults (no explicit resolution/bitrate/codec override
 *   yet) - a later step should add configurable export settings.
 */
@OptIn(UnstableApi::class)
class ExportEngine(private val context: Context) {

    fun export(sourceUri: Uri, clips: List<Clip>): Flow<ExportProgress> = callbackFlow {
        if (clips.isEmpty()) {
            trySend(ExportProgress.Failure("Nothing to export - the timeline is empty."))
            close()
            return@callbackFlow
        }

        val outputFile = File(
            context.getExternalFilesDir(null),
            "carl_export_${System.currentTimeMillis()}.mp4"
        )

        val editedItems = clips.map { clip ->
            val mediaItem = MediaItem.Builder()
                .setUri(sourceUri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(clip.sourceStartMs)
                        .setEndPositionMs(clip.sourceEndMs)
                        .build()
                )
                .build()
            EditedMediaItem.Builder(mediaItem).build()
        }

        val composition = Composition.Builder(EditedMediaItemSequence(editedItems)).build()

        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(finishedComposition: Composition, exportResult: ExportResult) {
                    trySend(ExportProgress.Success(Uri.fromFile(outputFile)))
                    close()
                }

                override fun onError(
                    finishedComposition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    // Never fail silently - surface the real error message to the caller.
                    trySend(ExportProgress.Failure(exportException.message ?: "Export failed"))
                    close()
                }
            })
            .build()

        transformer.start(composition, outputFile.absolutePath)

        // Transformer has no push-based progress callback - it must be polled.
        val progressHolder = ProgressHolder()
        while (isActive) {
            val state = transformer.getProgress(progressHolder)
            if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                trySend(ExportProgress.InProgress(progressHolder.progress))
            }
            delay(250)
        }

        awaitClose { transformer.cancel() }
    }
}
