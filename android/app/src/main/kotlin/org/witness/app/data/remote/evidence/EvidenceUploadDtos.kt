package org.witness.app.data.remote.evidence

data class RegisterHashRequest(
    val evidenceId: String,
    val hash: String,
    val timestamp: String,
    val metadata: EvidenceMetadataPayload,
)

data class EvidenceMetadataPayload(
    val appVersion: String,
    val captureMode: String,
    val mediaType: String,
    val device: DevicePayload,
    val location: LocationPayload?,
    val timeSource: String,
)

data class DevicePayload(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val fingerprint: String,
)

data class LocationPayload(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracyMeters: Float,
    val provider: String,
)

data class RegisterHashResponse(
    val evidenceId: String,
    val hashReceivedAt: String,
    val accepted: Boolean,
)

data class UploadChunkResponse(
    val evidenceId: String,
    val chunkIndex: Int,
    val chunkHash: String,
    val receivedAt: String,
    val accepted: Boolean,
)
