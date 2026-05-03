package org.witness.app.ui.camouflage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {
    private val engine = CalculatorEngine()

    @Test
    fun evaluatesBasicAddition() {
        val result = enter("12+7=")

        assertEquals("19", result.display)
        assertFalse(result.unlocked)
    }

    @Test
    fun returnsErrorForDivisionByZero() {
        val result = enter("4/0=")

        assertEquals("Error", result.display)
        assertFalse(result.unlocked)
    }

    @Test
    fun unlocksOnSecretExpression() {
        val result = enter("1312=")

        assertEquals("0", result.display)
        assertTrue(result.unlocked)
    }

    private fun enter(expression: String): CalculatorResult {
        var result = CalculatorResult(display = "0", unlocked = false)
        expression.forEach { token ->
            result = engine.input(result.display, token.toString())
        }
        return result
    }
}
