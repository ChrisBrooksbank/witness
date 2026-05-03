package org.witness.app.domain.safety

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WitnessModeArmControllerTest {
    @Test
    fun armsWithFiveSecondCancelWindow() {
        val controller = WitnessModeArmController()

        val state = controller.arm(10_000L)

        assertEquals(15_000L, state.activatesAtMillis)
        assertFalse(controller.activationDue(14_999L))
        assertTrue(controller.activationDue(15_000L))
    }

    @Test
    fun cancelPreventsActivation() {
        val controller = WitnessModeArmController()

        controller.arm(10_000L)
        controller.cancel()

        assertFalse(controller.activationDue(20_000L))
    }
}
