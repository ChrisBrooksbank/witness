package org.witness.app.service.capture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.witness.app.domain.model.RecordingState

object CaptureServiceState {
    private val mutableState = MutableStateFlow<RecordingState>(RecordingState.Idle)

    val state: StateFlow<RecordingState> = mutableState.asStateFlow()

    fun update(state: RecordingState) {
        mutableState.value = state
    }
}
