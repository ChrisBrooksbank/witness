package org.witness.app.domain.safety

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WitnessModeSequenceDetectorTest {
    @Test
    fun matchesUpUpDownDownWithinTimingWindow() {
        val detector = WitnessModeSequenceDetector()

        assertFalse(detector.recordPress(VolumeButton.Up, 1_000L))
        assertFalse(detector.recordPress(VolumeButton.Up, 1_200L))
        assertFalse(detector.recordPress(VolumeButton.Down, 1_400L))
        assertTrue(detector.recordPress(VolumeButton.Down, 1_600L))
    }

    @Test
    fun resetsWhenGapExceedsTimingWindow() {
        val detector = WitnessModeSequenceDetector()

        assertFalse(detector.recordPress(VolumeButton.Up, 1_000L))
        assertFalse(detector.recordPress(VolumeButton.Up, 1_700L))
        assertFalse(detector.recordPress(VolumeButton.Down, 1_800L))
        assertFalse(detector.recordPress(VolumeButton.Down, 1_900L))
    }
}
