package com.carl.editor.device

import android.app.ActivityManager
import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build

/**
 * Conservative device profile used to choose editor preview/export workloads.
 *
 * This is intentionally capability-based rather than model-name based. Hardware varies
 * substantially between devices with similar names, and Android recommends runtime capability
 * checks rather than device allowlists for adaptive behavior.
 */
enum class EditorPerformanceTier {
    LOW,
    BALANCED,
    HIGH
}

data class DeviceCapabilityProfile(
    val performanceTier: EditorPerformanceTier,
    val memoryClassMb: Int,
    val supportsAvcEncoder: Boolean,
    val supportsHevcEncoder: Boolean,
    val supportsAv1Encoder: Boolean,
    val isLowRamDevice: Boolean
) {
    val recommendedPreviewLongEdgePx: Int
        get() = when (performanceTier) {
            EditorPerformanceTier.LOW -> 720
            EditorPerformanceTier.BALANCED -> 1080
            EditorPerformanceTier.HIGH -> 1440
        }

    val maxRecommendedExportWidthPx: Int
        get() = when (performanceTier) {
            EditorPerformanceTier.LOW -> 1920
            EditorPerformanceTier.BALANCED -> 2560
            EditorPerformanceTier.HIGH -> 3840
        }

    companion object {
        fun from(context: Context): DeviceCapabilityProfile {
            val activityManager = context.getSystemService(ActivityManager::class.java)
            val memoryClassMb = activityManager?.memoryClass ?: 0
            val isLowRam = activityManager?.isLowRamDevice == true

            val encoders = runCatching {
                MediaCodecList(MediaCodecList.ALL_CODECS)
                    .codecInfos
                    .filter { it.isEncoder }
            }.getOrDefault(emptyList())

            fun supports(mime: String): Boolean =
                encoders.any { info ->
                    info.supportedTypes.any { it.equals(mime, ignoreCase = true) }
                }

            val avc = supports("video/avc")
            val hevc = supports("video/hevc")
            val av1 = if (Build.VERSION.SDK_INT >= 29) supports("video/av01") else false

            val tier = when {
                isLowRam || memoryClassMb in 1..192 -> EditorPerformanceTier.LOW
                memoryClassMb >= 512 && (hevc || av1) -> EditorPerformanceTier.HIGH
                else -> EditorPerformanceTier.BALANCED
            }

            return DeviceCapabilityProfile(
                performanceTier = tier,
                memoryClassMb = memoryClassMb,
                supportsAvcEncoder = avc,
                supportsHevcEncoder = hevc,
                supportsAv1Encoder = av1,
                isLowRamDevice = isLowRam
            )
        }
    }
}
