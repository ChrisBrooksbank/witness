package org.witness.app.service.capture

import org.junit.Assert.assertEquals
import org.junit.Test
import org.witness.app.domain.model.MediaType

class CaptureOutputFilesTest {
    @Test
    fun createsMp4NameForVideoEvidence() {
        assertEquals(
            "evidence-123-42.mp4",
            CaptureOutputFiles.fileName("evidence-123", MediaType.Video, 42),
        )
    }

    @Test
    fun createsM4aNameForAudioEvidence() {
        assertEquals(
            "witness-99-42.m4a",
            CaptureOutputFiles.fileName("witness-99", MediaType.Audio, 42),
        )
    }

    @Test
    fun sanitizesEvidenceIdForFileSystemUse() {
        assertEquals(
            "bad____id-42.mp4",
            CaptureOutputFiles.fileName("bad/../id", MediaType.Video, 42),
        )
    }
}
