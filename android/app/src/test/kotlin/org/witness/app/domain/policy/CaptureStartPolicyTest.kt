package org.witness.app.domain.policy

import org.junit.Assert.assertEquals
import org.junit.Test
import org.witness.app.domain.model.CaptureMode
import org.witness.app.domain.model.MediaType

class CaptureStartPolicyTest {
    private val policy = CaptureStartPolicy()

    @Test
    fun keepsRequestedVideoCaptureWhenBatteryIsHealthy() {
        assertEquals(
            CaptureStartDecision.Start(CaptureMode.Standard, MediaType.Video),
            policy.decisionFor(
                requestedMode = CaptureMode.Standard,
                requestedMediaType = MediaType.Video,
                batteryAction = BatteryCaptureAction.KeepVideo,
            ),
        )
    }

    @Test
    fun switchesVideoRequestsToAudioOnlyWhenBatteryIsLow() {
        assertEquals(
            CaptureStartDecision.Start(CaptureMode.AudioOnly, MediaType.Audio),
            policy.decisionFor(
                requestedMode = CaptureMode.Witness,
                requestedMediaType = MediaType.Video,
                batteryAction = BatteryCaptureAction.SwitchToAudioOnly,
            ),
        )
    }

    @Test
    fun stopsCaptureWhenBatteryIsCritical() {
        assertEquals(
            CaptureStartDecision.StopGracefully,
            policy.decisionFor(
                requestedMode = CaptureMode.Standard,
                requestedMediaType = MediaType.Video,
                batteryAction = BatteryCaptureAction.StopGracefully,
            ),
        )
    }
}
