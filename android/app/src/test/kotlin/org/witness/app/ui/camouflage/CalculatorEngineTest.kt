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

    @Test
    fun repeatedEqualsOnComputedSecretDoesNotUnlock() {
        val result = enter("1300+12==")

        assertEquals("1312", result.display)
        assertFalse(result.unlocked)
    }

    @Test
    fun digitAfterResultStartsNewExpression() {
        val result = enter("2+3=", start = enter("1300+12="))

        assertEquals("5", result.display)
    }

    @Test
    fun unlocksWhenSecretTypedAfterResult() {
        val result = enter("1312=", start = enter("2+3="))

        assertTrue(result.unlocked)
    }

    @Test
    fun chainsOperationsOnNegativeResult() {
        val result = enter("+5=", start = enter("3-5="))

        assertEquals("3", result.display)
    }

    @Test
    fun formatsLargeResultsWithoutOverflow() {
        val result = enter("99999999999*99999999999=")

        assertEquals((99999999999.0 * 99999999999.0).toString(), result.display)
    }

    private fun enter(
        expression: String,
        start: CalculatorResult = CalculatorResult(display = "0", unlocked = false),
    ): CalculatorResult {
        var result = start
        expression.forEach { token ->
            result = engine.input(result.display, token.toString())
        }
        return result
    }
}
