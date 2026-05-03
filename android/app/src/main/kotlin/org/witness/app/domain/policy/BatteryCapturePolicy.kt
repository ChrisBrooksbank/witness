package org.witness.app.domain.policy

private const val LOW_BATTERY_THRESHOLD_PERCENT = 15
private const val CRITICAL_BATTERY_THRESHOLD_PERCENT = 5

class BatteryCapturePolicy {
    fun actionForBatteryPercent(percent: Int): BatteryCaptureAction {
        return when {
            percent <= CRITICAL_BATTERY_THRESHOLD_PERCENT -> BatteryCaptureAction.StopGracefully
            percent < LOW_BATTERY_THRESHOLD_PERCENT -> BatteryCaptureAction.SwitchToAudioOnly
            else -> BatteryCaptureAction.KeepVideo
        }
    }
}

enum class BatteryCaptureAction {
    KeepVideo,
    SwitchToAudioOnly,
    StopGracefully,
}
