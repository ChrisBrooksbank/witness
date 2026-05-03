package org.witness.app.data.local.evidence

import org.junit.Assert.assertEquals
import org.junit.Test

class EvidenceEntityTest {
    @Test
    fun uploadConfirmationSetsDeletionDeadlineAfterGracePeriod() {
        val confirmedAt = 1_000L
        val confirmed = sampleEvidence().withUploadConfirmed(confirmedAt)

        assertEquals(UploadStatus.Complete.name, confirmed.uploadStatus)
        assertEquals(confirmedAt, confirmed.confirmedUploadedAtEpochMillis)
        assertEquals(EvidenceEntity.deletionDeadline(confirmedAt), confirmed.deleteAfterEpochMillis)
    }

    private fun sampleEvidence(): EvidenceEntity {
        return EvidenceEntity(
            id = "evidence-1",
            mediaType = "Video",
            captureMode = "Standard",
            merkleRoot = null,
            capturedAtEpochMillis = 0L,
            deviceManufacturer = "Google",
            deviceModel = "Pixel",
            androidVersion = "14",
            appVersion = "0.1.0",
            latitude = null,
            longitude = null,
            locationAccuracyMeters = null,
            locationUnavailableReason = "location unavailable",
        )
    }
}
