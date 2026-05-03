package org.witness.app.ui.camouflage

private const val SECRET_UNLOCK_EXPRESSION = "1312="
private const val ERROR_VALUE = "Error"
private const val ZERO_VALUE = "0"

class CalculatorEngine {
    fun input(currentDisplay: String, token: String): CalculatorResult {
        val expression = if (currentDisplay == ZERO_VALUE || currentDisplay == ERROR_VALUE) "" else currentDisplay

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
        val operator = expression.firstOrNull { value -> value in listOf('+', '-', '*', '/') }
        val parts = operator?.let { expression.split(it) }.orEmpty()
        val left = parts.getOrNull(0)?.toDoubleOrNull()
        val right = parts.getOrNull(1)?.toDoubleOrNull()

        val result = when {
            operator == null -> expression
            parts.size != 2 -> null
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
        return if (this % 1.0 == 0.0) toLong().toString() else toString()
    }
}

data class CalculatorResult(
    val display: String,
    val unlocked: Boolean,
)
