package org.witness.app.domain.safety

private const val DEFAULT_CANCEL_WINDOW_MILLIS = 5_000L

data class WitnessModeArmState(
    val armedAtMillis: Long,
    val activatesAtMillis: Long,
) {
    fun isActive(nowMillis: Long): Boolean = nowMillis >= activatesAtMillis
}

class WitnessModeArmController(
    private val cancelWindowMillis: Long = DEFAULT_CANCEL_WINDOW_MILLIS,
) {
    private var armedState: WitnessModeArmState? = null

    val isArmed: Boolean
        get() = armedState != null

    fun arm(nowMillis: Long): WitnessModeArmState {
        return WitnessModeArmState(
            armedAtMillis = nowMillis,
            activatesAtMillis = nowMillis + cancelWindowMillis,
        ).also { armedState = it }
    }

    fun cancel() {
        armedState = null
    }

    fun activationDue(nowMillis: Long): Boolean {
        val state = armedState ?: return false
        val due = state.isActive(nowMillis)
        if (due) {
            armedState = null
        }
        return due
    }
}
