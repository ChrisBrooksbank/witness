package org.witness.app.domain.model

import java.time.Instant

private const val DEFAULT_VIDEO_WIDTH = 1280
private const val DEFAULT_VIDEO_HEIGHT = 720
private const val DEFAULT_VIDEO_FRAME_RATE = 30
private const val DEFAULT_AUDIO_BITRATE = 128_000

enum class MediaType {
    Video,
    Audio,
    Photo,
}

enum class CaptureMode {
    Standard,
    Witness,
    LowBattery,
    AudioOnly,
}

data class CaptureQuality(
    val width: Int,
    val height: Int,
    val frameRate: Int,
    val audioBitrate: Int,
) {
    companion object {
        val DefaultVideo = CaptureQuality(
            width = DEFAULT_VIDEO_WIDTH,
            height = DEFAULT_VIDEO_HEIGHT,
            frameRate = DEFAULT_VIDEO_FRAME_RATE,
            audioBitrate = DEFAULT_AUDIO_BITRATE,
        )
    }
}

sealed interface RecordingState {
    data object Idle : RecordingState

    data class Active(
        val evidenceId: String,
        val startedAt: Instant,
        val mode: CaptureMode,
        val mediaType: MediaType,
        val quality: CaptureQuality,
    ) : RecordingState

    data class Stopping(
        val evidenceId: String,
        val requestedAt: Instant,
    ) : RecordingState

    data class Error(
        val message: String,
        val occurredAt: Instant,
    ) : RecordingState
}
