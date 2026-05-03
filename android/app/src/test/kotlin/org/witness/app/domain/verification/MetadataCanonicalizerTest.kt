package org.witness.app.domain.verification

import java.time.Instant
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.witness.app.domain.model.CaptureMode
import org.witness.app.domain.model.DeviceInfo
import org.witness.app.domain.model.EvidenceChunkHash
import org.witness.app.domain.model.EvidenceLocation
import org.witness.app.domain.model.EvidenceMetadata
import org.witness.app.domain.model.MediaType
import org.witness.app.domain.model.TimeSource

class MetadataCanonicalizerTest {
    private val canonicalizer = MetadataCanonicalizer()

    @Test
    fun canonicalizationIsStableForEquivalentMetadata() {
        val first = sampleMetadata(chunkOrder = listOf(1, 0))
        val second = sampleMetadata(chunkOrder = listOf(0, 1))

        assertArrayEquals(canonicalizer.canonicalize(first), canonicalizer.canonicalize(second))
    }

    @Test
    fun canonicalizationChangesWhenMetadataChanges() {
        val first = sampleMetadata(appVersion = "0.1.0")
        val second = sampleMetadata(appVersion = "0.1.1")

        assertNotEquals(
            canonicalizer.canonicalize(first).decodeToString(),
            canonicalizer.canonicalize(second).decodeToString(),
        )
    }

    @Test
    fun canonicalizationIncludesLocationUnavailableReason() {
        val metadata = sampleMetadata(location = null)
        val canonical = canonicalizer.canonicalize(metadata).decodeToString()

        assertTrue(canonical.contains("locationUnavailableReason=location unavailable"))
    }

    private fun sampleMetadata(
        appVersion: String = "0.1.0",
        chunkOrder: List<Int> = listOf(0, 1),
        location: EvidenceLocation? = sampleLocation(),
    ): EvidenceMetadata {
        val capturedAt = Instant.parse("2026-05-03T12:00:00Z")

        return EvidenceMetadata(
            evidenceId = "evidence-1",
            merkleRoot = "root",
            chunkHashes = chunkOrder.map { index -> sampleChunk(index, capturedAt) },
            capturedAt = capturedAt,
            networkCapturedAt = Instant.parse("2026-05-03T12:00:01Z"),
            timeSource = TimeSource.Network,
            location = location,
            locationUnavailableReason = if (location == null) "location unavailable" else null,
            device = DeviceInfo(
                manufacturer = "Google",
                model = "Pixel",
                androidVersion = "14",
                fingerprint = "fingerprint",
            ),
            appVersion = appVersion,
            mediaType = MediaType.Video,
            captureMode = CaptureMode.Witness,
            orientationDegrees = 90,
            signature = null,
        )
    }

    private fun sampleChunk(index: Int, capturedAt: Instant): EvidenceChunkHash {
        return EvidenceChunkHash(
            evidenceId = "evidence-1",
            chunkIndex = index,
            sha256 = "hash-$index",
            capturedAt = capturedAt.plusSeconds(index.toLong()),
            sizeBytes = 1024L + index,
        )
    }

    private fun sampleLocation(): EvidenceLocation {
        return EvidenceLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            altitude = null,
            accuracyMeters = 8.5f,
            provider = "gps",
        )
    }
}
