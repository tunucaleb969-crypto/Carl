package com.carl.editor.export

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

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
 * - Honors each clip's playback speed using Media3's EditedMediaItem.Builder#setSpeed(SpeedProvider),
 *   so the exported media duration and audio/video timing follow the same speed value as preview.
 * - Does NOT bake in rotate/flip or color adjustments yet. Those remain preview-only until export
 *   receives the same effect state from the editor.
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

        validateSource(sourceUri)?.let { message ->
            trySend(ExportProgress.Failure(message))
            close()
            return@callbackFlow
        }

        clips.firstOrNull { it.sourceStartMs < 0 || it.sourceEndMs <= it.sourceStartMs }?.let {
            trySend(ExportProgress.Failure("Timeline contains a clip with an invalid trim range."))
            close()
            return@callbackFlow
        }

        val outputFile = createOutputFile()
        if (outputFile == null) {
            trySend(ExportProgress.Failure("Unable to create an export output directory."))
            close()
            return@callbackFlow
        }

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
            EditedMediaItem.Builder(mediaItem)
                .setSpeed(ConstantSpeedProvider(clip.speed))
                .build()
        }

        val composition = Composition.Builder(EditedMediaItemSequence(editedItems)).build()
        val isTerminal = AtomicBoolean(false)
        val completedSuccessfully = AtomicBoolean(false)
        val mainHandler = Handler(Looper.getMainLooper())

        // Media3 Transformer confines its methods and listeners to one application thread.
        // Build and start it on the main thread, then use that same dispatcher for polling.
        val transformer = withContext(NonCancellable + Dispatchers.Main.immediate) {
            Transformer.Builder(context)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(finishedComposition: Composition, exportResult: ExportResult) {
                        if (isTerminal.compareAndSet(false, true)) {
                            val result = trySend(ExportProgress.Success(Uri.fromFile(outputFile)))
                            completedSuccessfully.set(result.isSuccess)
                            close()
                        }
                    }

                    override fun onError(
                        finishedComposition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        if (isTerminal.compareAndSet(false, true)) {
                            // Never fail silently - surface the real error message to the caller.
                            trySend(ExportProgress.Failure(exportException.message ?: "Export failed"))
                            close()
                        }
                    }
                })
                .build()
                .also { transformer ->
                    try {
                        transformer.start(composition, outputFile.absolutePath)
                    } catch (exception: RuntimeException) {
                        isTerminal.set(true)
                        trySend(ExportProgress.Failure(exception.message ?: "Unable to start export"))
                        close()
                    }
                }
        }

        // Transformer has no push-based progress callback - it must be polled.
        val progressJob = launch(Dispatchers.Main.immediate) {
            val progressHolder = ProgressHolder()
            try {
                while (!isTerminal.get()) {
                    val state = transformer.getProgress(progressHolder)
                    if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                        trySend(ExportProgress.InProgress(progressHolder.progress))
                    }
                    delay(250)
                }
            } catch (exception: RuntimeException) {
                if (isTerminal.compareAndSet(false, true)) {
                    trySend(ExportProgress.Failure(exception.message ?: "Unable to read export progress"))
                    close()
                }
            }
        }

        awaitClose {
            progressJob.cancel()
            mainHandler.post {
                if (!isTerminal.getAndSet(true)) {
                    transformer.cancel()
                }
                if (!completedSuccessfully.get()) {
                    outputFile.delete()
                }
            }
        }
    }

    private class ConstantSpeedProvider(private val speed: Float) : SpeedProvider {
        override fun getNextSpeedChangeTimeUs(timeUs: Long): Long = C.TIME_UNSET

        override fun getSpeed(timeUs: Long): Float = speed
    }

    private fun validateSource(sourceUri: Uri): String? = when (sourceUri.scheme) {
        "file" -> {
            val sourceFile = sourceUri.path?.let(::File)
            if (sourceFile == null || !sourceFile.isFile || !sourceFile.canRead()) {
                "The selected source file is missing or cannot be read."
            } else {
                null
            }
        }

        "content" -> try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                "The selected source cannot be read."
            } else {
                inputStream.use { }
                null
            }
        } catch (exception: SecurityException) {
            "Permission to read the selected source was denied."
        } catch (exception: Exception) {
            "The selected source cannot be read."
        }

        null -> "The selected source has no URI scheme."
        else -> null
    }

    private fun createOutputFile(): File? {
        val outputDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            return null
        }
        if (!outputDirectory.isDirectory || !outputDirectory.canWrite()) {
            return null
        }
        return File(outputDirectory, "carl_export_${System.currentTimeMillis()}.mp4")
    }
}
