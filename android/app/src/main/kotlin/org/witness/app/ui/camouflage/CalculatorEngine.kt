package org.witness.app.ui.camouflage

import kotlin.math.abs

private const val SECRET_UNLOCK_EXPRESSION = "1312="
private const val ERROR_VALUE = "Error"
private const val ZERO_VALUE = "0"
private const val MAX_EXACT_INTEGER_RESULT = 1e15
private val OPERATORS = setOf('+', '-', '*', '/')

class CalculatorEngine {
    private var showingResult = false

    fun input(currentDisplay: String, token: String): CalculatorResult {
        val startsFresh = currentDisplay == ZERO_VALUE ||
            currentDisplay == ERROR_VALUE ||
            (showingResult && token.firstOrNull()?.isDigit() == true)
        val expression = if (startsFresh) "" else currentDisplay

        // "=" on a computed result must not re-evaluate it, otherwise 1300+12 "=" "=" would unlock.
        if (token == "=" && showingResult) return CalculatorResult(display = currentDisplay, unlocked = false)

        showingResult = token == "="
        return when {
            token == "C" -> CalculatorResult(display = ZERO_VALUE, unlocked = false)
            token == "=" -> evaluateInput("$expression=")
            else -> CalculatorResult(display = expression + token, unlocked = false)
        }
    }

    private fun evaluateInput(expressionWithEquals: String): CalculatorResult {
        return if (expressionWithEquals == SECRET_UNLOCK_EXPRESSION) {
            CalculatorResult(display = ZERO_VALUE, unlocked = true)
        } else {
            val expression = expressionWithEquals.removeSuffix("=")
            CalculatorResult(display = evaluate(expression) ?: ERROR_VALUE, unlocked = false)
        }
    }

    private fun evaluate(expression: String): String? {
        // Skip index 0 so a negative first operand (e.g. a previous "-2" result) is not read as the operator.
        val operatorIndex = (1 until expression.length).firstOrNull { index -> expression[index] in OPERATORS }
        val operator = operatorIndex?.let { expression[it] }
        val left = operatorIndex?.let { expression.substring(0, it).toDoubleOrNull() }
        val right = operatorIndex?.let { expression.substring(it + 1).toDoubleOrNull() }

        val result = when {
            operator == null -> expression
            left == null || right == null -> null
            operator == '+' -> (left + right).formatResult()
            operator == '-' -> (left - right).formatResult()
            operator == '*' -> (left * right).formatResult()
            operator == '/' && right != 0.0 -> (left / right).formatResult()
            else -> null
        }

        return result
    }

    private fun Double.formatResult(): String {
        return if (this % 1.0 == 0.0 && abs(this) < MAX_EXACT_INTEGER_RESULT) toLong().toString() else toString()
    }
}

data class CalculatorResult(
    val display: String,
    val unlocked: Boolean,
)
