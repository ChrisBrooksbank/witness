package org.witness.app.domain.policy

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryCapturePolicyTest {
    private val policy = BatteryCapturePolicy()

    @Test
    fun keepsVideoAtHealthyBatteryLevel() {
        assertEquals(BatteryCaptureAction.KeepVideo, policy.actionForBatteryPercent(50))
    }

    @Test
    fun switchesToAudioOnlyBelowLowBatteryThreshold() {
        assertEquals(BatteryCaptureAction.SwitchToAudioOnly, policy.actionForBatteryPercent(14))
    }

    @Test
    fun stopsGracefullyAtCriticalBatteryThreshold() {
        assertEquals(BatteryCaptureAction.StopGracefully, policy.actionForBatteryPercent(5))
    }
}
