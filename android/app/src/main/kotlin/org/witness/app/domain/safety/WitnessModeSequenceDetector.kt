package org.witness.app.domain.safety

private const val DEFAULT_MAX_GAP_MILLIS = 500L

enum class VolumeButton {
    Up,
    Down,
}

class WitnessModeSequenceDetector(
    private val maxGapMillis: Long = DEFAULT_MAX_GAP_MILLIS,
) {
    private val expectedPattern = listOf(
        VolumeButton.Up,
        VolumeButton.Up,
        VolumeButton.Down,
        VolumeButton.Down,
    )
    private val recentPresses = mutableListOf<VolumeButton>()
    private var lastPressAtMillis: Long? = null

    fun recordPress(button: VolumeButton, eventAtMillis: Long): Boolean {
        val lastPressAt = lastPressAtMillis
        if (lastPressAt != null && eventAtMillis - lastPressAt > maxGapMillis) {
            recentPresses.clear()
        }

        recentPresses += button
        lastPressAtMillis = eventAtMillis

        if (recentPresses.size > expectedPattern.size) {
            recentPresses.removeAt(0)
        }

        val matched = recentPresses == expectedPattern
        if (matched) {
            recentPresses.clear()
            lastPressAtMillis = null
        }
        return matched
    }
}
