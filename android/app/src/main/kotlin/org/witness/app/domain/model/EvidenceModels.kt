package org.witness.app.domain.model

import java.time.Instant

data class EvidenceChunkHash(
    val evidenceId: String,
    val chunkIndex: Int,
    val sha256: String,
    val capturedAt: Instant,
    val sizeBytes: Long,
)

data class EvidenceLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracyMeters: Float,
    val provider: String,
)

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val fingerprint: String,
)

enum class TimeSource {
    Device,
    Network,
}

data class EvidenceMetadata(
    val evidenceId: String,
    val merkleRoot: String,
    val chunkHashes: List<EvidenceChunkHash>,
    val capturedAt: Instant,
    val networkCapturedAt: Instant?,
    val timeSource: TimeSource,
    val location: EvidenceLocation?,
    val locationUnavailableReason: String?,
    val device: DeviceInfo,
    val appVersion: String,
    val mediaType: MediaType,
    val captureMode: CaptureMode,
    val orientationDegrees: Int,
    val signature: String?,
) {
    val hasLocation: Boolean
        get() = location != null
}
