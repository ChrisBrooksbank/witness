package org.witness.app.domain.verification

import org.witness.app.domain.model.DeviceInfo
import org.witness.app.domain.model.EvidenceChunkHash
import org.witness.app.domain.model.EvidenceLocation
import org.witness.app.domain.model.EvidenceMetadata

class MetadataCanonicalizer {
    fun canonicalize(metadata: EvidenceMetadata): ByteArray {
        return buildString {
            appendLine("appVersion=${metadata.appVersion}")
            appendLine("capturedAt=${metadata.capturedAt}")
            appendLine("captureMode=${metadata.captureMode.name}")
            appendLine("chunkHashes=${canonicalChunkHashes(metadata.chunkHashes)}")
            appendLine("device=${canonicalDevice(metadata.device)}")
            appendLine("evidenceId=${metadata.evidenceId}")
            appendLine("location=${canonicalLocation(metadata.location)}")
            appendLine("locationUnavailableReason=${metadata.locationUnavailableReason.orEmpty()}")
            appendLine("mediaType=${metadata.mediaType.name}")
            appendLine("merkleRoot=${metadata.merkleRoot}")
            appendLine("networkCapturedAt=${metadata.networkCapturedAt ?: ""}")
            appendLine("orientationDegrees=${metadata.orientationDegrees}")
            appendLine("timeSource=${metadata.timeSource.name}")
        }.encodeToByteArray()
    }

    private fun canonicalChunkHashes(chunkHashes: List<EvidenceChunkHash>): String {
        return chunkHashes
            .sortedWith(compareBy(EvidenceChunkHash::chunkIndex, EvidenceChunkHash::sha256))
            .joinToString(separator = "|") { chunk ->
                listOf(
                    chunk.chunkIndex.toString(),
                    chunk.sha256,
                    chunk.capturedAt.toString(),
                    chunk.sizeBytes.toString(),
                ).joinToString(separator = ",")
            }
    }

    private fun canonicalDevice(device: DeviceInfo): String {
        return listOf(
            device.manufacturer,
            device.model,
            device.androidVersion,
            device.fingerprint,
        ).joinToString(separator = ",")
    }

    private fun canonicalLocation(location: EvidenceLocation?): String {
        if (location == null) return ""

        return listOf(
            location.latitude.toString(),
            location.longitude.toString(),
            location.altitude?.toString().orEmpty(),
            location.accuracyMeters.toString(),
            location.provider,
        ).joinToString(separator = ",")
    }
}
