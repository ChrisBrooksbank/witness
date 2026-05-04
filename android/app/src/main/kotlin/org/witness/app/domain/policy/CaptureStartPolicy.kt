package org.witness.app.domain.policy

import org.witness.app.domain.model.CaptureMode
import org.witness.app.domain.model.MediaType

class CaptureStartPolicy {
    fun decisionFor(
        requestedMode: CaptureMode,
        requestedMediaType: MediaType,
        batteryAction: BatteryCaptureAction,
    ): CaptureStartDecision {
        return when (batteryAction) {
            BatteryCaptureAction.KeepVideo -> CaptureStartDecision.Start(
                captureMode = requestedMode,
                mediaType = requestedMediaType,
            )

            BatteryCaptureAction.SwitchToAudioOnly -> CaptureStartDecision.Start(
                captureMode = CaptureMode.AudioOnly,
                mediaType = MediaType.Audio,
            )

            BatteryCaptureAction.StopGracefully -> CaptureStartDecision.StopGracefully
        }
    }
}

sealed interface CaptureStartDecision {
    data class Start(
        val captureMode: CaptureMode,
        val mediaType: MediaType,
    ) : CaptureStartDecision

    data object StopGracefully : CaptureStartDecision
}
