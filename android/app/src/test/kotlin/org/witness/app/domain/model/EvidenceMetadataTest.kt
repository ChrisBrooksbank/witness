package org.witness.app.domain.model

import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceMetadataTest {
    @Test
    fun metadataReportsMissingLocation() {
        val metadata = sampleMetadata(location = null)

        assertFalse(metadata.hasLocation)
    }

    @Test
    fun metadataReportsAvailableLocation() {
        val metadata = sampleMetadata(
            location = EvidenceLocation(
                latitude = 40.7128,
                longitude = -74.0060,
                altitude = null,
                accuracyMeters = 8.5f,
                provider = "gps",
            ),
        )

        assertTrue(metadata.hasLocation)
    }

    private fun sampleMetadata(location: EvidenceLocation?): EvidenceMetadata {
        val capturedAt = Instant.parse("2026-05-03T12:00:00Z")

        return EvidenceMetadata(
            evidenceId = "evidence-1",
            merkleRoot = "abc123",
            chunkHashes = emptyList(),
            capturedAt = capturedAt,
            networkCapturedAt = null,
            timeSource = TimeSource.Device,
            location = location,
            locationUnavailableReason = if (location == null) "location unavailable" else null,
            device = DeviceInfo(
                manufacturer = "Google",
                model = "Pixel",
                androidVersion = "14",
                fingerprint = "test-fingerprint",
            ),
            appVersion = "0.1.0",
            mediaType = MediaType.Video,
            captureMode = CaptureMode.Standard,
            orientationDegrees = 0,
            signature = null,
        )
    }
}
