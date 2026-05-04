@file:Suppress("ktlint:standard:function-naming")

package org.witness.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.witness.app.R
import org.witness.app.domain.model.CaptureMode
import org.witness.app.domain.model.MediaType
import org.witness.app.domain.model.RecordingState
import org.witness.app.service.capture.CaptureService
import org.witness.app.service.capture.CaptureServiceState
import org.witness.app.ui.theme.WitnessTheme

private val ScreenPadding = 24.dp
private val SectionSpacing = 24.dp
private val StatusPadding = 12.dp
private val IconSpacing = 8.dp
private val StatusIconSize = 18.dp
private val RecordButtonSize = 128.dp
private val RecordIconSize = 52.dp
private val UploadIconSize = 28.dp

private enum class MainDestination(
    val labelRes: Int,
) {
    Home(R.string.home),
    Queue(R.string.queue),
    Settings(R.string.settings),
}

private sealed class RecordingUiState(
    val labelRes: Int,
    val descriptionRes: Int,
    val color: Color,
) {
    data object Ready : RecordingUiState(
        labelRes = R.string.ready,
        descriptionRes = R.string.ready_description,
        color = Color.Black,
    )

    data object Recording : RecordingUiState(
        labelRes = R.string.recording_status,
        descriptionRes = R.string.recording_status,
        color = Color.Red,
    )
}

@Composable
@Suppress("FunctionName")
fun WitnessApp() {
    val context = LocalContext.current
    var selectedDestination by remember { mutableStateOf(MainDestination.Home) }
    val serviceRecordingState by CaptureServiceState.state.collectAsState()
    val recordingState = serviceRecordingState.toRecordingUiState()
    var hasAcceptedDisclaimer by remember { mutableStateOf(false) }
    var wifiOnlyUploads by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RecordingStatusBar(recordingState = recordingState)
        },
        bottomBar = {
            MainNavigationBar(
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (selectedDestination) {
                MainDestination.Home -> RecordingHomeScreen(
                    recordingState = recordingState,
                    onRecordToggle = {
                        when (serviceRecordingState) {
                            is RecordingState.Active -> context.startService(CaptureService.stopIntent(context))
                            else -> {
                                val intent = CaptureService.startIntent(
                                    context = context,
                                    evidenceId = "evidence-${System.currentTimeMillis()}",
                                    captureMode = CaptureMode.Standard,
                                    mediaType = MediaType.Video,
                                )
                                ContextCompat.startForegroundService(context, intent)
                            }
                        }
                    },
                )

                MainDestination.Queue -> UploadQueueScreen()
                MainDestination.Settings -> SettingsScreen(
                    wifiOnlyUploads = wifiOnlyUploads,
                    onWifiOnlyUploadsChanged = { wifiOnlyUploads = it },
                )
            }
        }
    }

    if (!hasAcceptedDisclaimer) {
        LegalDisclaimerDialog(
            onAccepted = { hasAcceptedDisclaimer = true },
        )
    }
}

private fun RecordingState.toRecordingUiState(): RecordingUiState {
    return when (this) {
        is RecordingState.Active,
        is RecordingState.Stopping,
        -> RecordingUiState.Recording

        is RecordingState.Error,
        RecordingState.Idle,
        -> RecordingUiState.Ready
    }
}

@Composable
@Suppress("FunctionName")
private fun RecordingStatusBar(recordingState: RecordingUiState) {
    val statusLabel = stringResource(recordingState.labelRes)
    val statusDescription = stringResource(R.string.status_description, statusLabel)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(recordingState.color)
            .padding(StatusPadding)
            .semantics {
                contentDescription = statusDescription
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(StatusIconSize)
                .background(Color.White, CircleShape),
        )
        Spacer(modifier = Modifier.width(IconSpacing))
        Text(
            text = statusLabel,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
@Suppress("FunctionName")
private fun RecordingHomeScreen(recordingState: RecordingUiState, onRecordToggle: () -> Unit) {
    val isRecording = recordingState == RecordingUiState.Recording
    val actionLabel = if (isRecording) R.string.stop else R.string.record
    val actionDescription = if (isRecording) R.string.stop else R.string.record_evidence
    val actionContentDescription = stringResource(actionDescription)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(recordingState.descriptionRes),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(SectionSpacing))
        ElevatedButton(
            onClick = onRecordToggle,
            modifier = Modifier
                .size(RecordButtonSize)
                .semantics {
                    contentDescription = actionContentDescription
                    role = Role.Button
                },
            shape = CircleShape,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else Color.Black,
                contentColor = Color.White,
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier.size(RecordIconSize),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White, CircleShape),
                    )
                }
                Text(
                    text = stringResource(actionLabel),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(modifier = Modifier.height(SectionSpacing))
        WitnessModeHint()
    }
}

@Composable
@Suppress("FunctionName")
private fun WitnessModeHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusGlyph()
        Spacer(modifier = Modifier.width(IconSpacing))
        Column {
            Text(
                text = stringResource(R.string.witness_mode),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.witness_mode_hint),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
@Suppress("FunctionName")
private fun UploadQueueScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        QueueGlyph()
        Spacer(modifier = Modifier.height(IconSpacing))
        Text(
            text = stringResource(R.string.upload_status),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
@Suppress("FunctionName")
private fun SettingsScreen(wifiOnlyUploads: Boolean, onWifiOnlyUploadsChanged: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = wifiOnlyUploads,
                onCheckedChange = onWifiOnlyUploadsChanged,
            )
            Spacer(modifier = Modifier.width(IconSpacing))
            Column {
                Text(
                    text = stringResource(R.string.wifi_only_uploads),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.wifi_only_uploads_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
@Suppress("FunctionName")
private fun LegalDisclaimerDialog(onAccepted: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = stringResource(R.string.legal_disclaimer_title))
        },
        text = {
            Text(text = stringResource(R.string.legal_disclaimer_body))
        },
        confirmButton = {
            TextButton(onClick = onAccepted) {
                Text(text = stringResource(R.string.i_understand))
            }
        },
    )
}

@Composable
@Suppress("FunctionName")
private fun MainNavigationBar(selectedDestination: MainDestination, onDestinationSelected: (MainDestination) -> Unit) {
    NavigationBar(containerColor = Color.Black) {
        MainDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    NavigationGlyph(selected = selectedDestination == destination)
                },
                label = {
                    Text(text = stringResource(destination.labelRes))
                },
            )
        }
    }
}

@Composable
private fun StatusGlyph() {
    Box(
        modifier = Modifier
            .size(UploadIconSize)
            .background(Color.Black, CircleShape),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(StatusIconSize)
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
private fun QueueGlyph() {
    Box(
        modifier = Modifier
            .size(RecordIconSize)
            .background(Color.Black, CircleShape),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(UploadIconSize)
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
private fun NavigationGlyph(selected: Boolean) {
    val glyphColor = if (selected) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(StatusIconSize)
            .background(glyphColor, CircleShape),
    )
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun WitnessAppPreview() {
    WitnessTheme {
        WitnessApp()
    }
}
