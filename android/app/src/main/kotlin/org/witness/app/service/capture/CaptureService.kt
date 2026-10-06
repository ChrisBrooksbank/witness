package org.witness.app.service.capture

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.witness.app.R
import org.witness.app.data.upload.CapturedEvidenceQueueRequest
import org.witness.app.data.upload.CapturedEvidenceQueuer
import org.witness.app.domain.model.CaptureMode
import org.witness.app.domain.model.CaptureQuality
import org.witness.app.domain.model.MediaType
import org.witness.app.domain.model.RecordingState
import org.witness.app.domain.policy.BatteryCapturePolicy
import org.witness.app.domain.policy.CaptureStartDecision
import org.witness.app.domain.policy.CaptureStartPolicy

private const val ACTION_START = "org.witness.app.action.START_CAPTURE"
private const val ACTION_STOP = "org.witness.app.action.STOP_CAPTURE"
private const val EXTRA_EVIDENCE_ID = "evidence_id"
private const val EXTRA_CAPTURE_MODE = "capture_mode"
private const val EXTRA_MEDIA_TYPE = "media_type"
private const val NOTIFICATION_ID = 1001
private const val CHANNEL_ID = "capture_status"
private const val FULL_BATTERY_SCALE_PERCENT = 100

@Suppress("TooManyFunctions")
class CaptureService : Service(), HardwareCaptureRecorder.Listener {
    private lateinit var recorder: HardwareCaptureRecorder
    private lateinit var evidenceQueuer: CapturedEvidenceQueuer
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val batteryCapturePolicy = BatteryCapturePolicy()
    private val captureStartPolicy = CaptureStartPolicy()
    private var activeRecording: RecordingState.Active? = null
    private var queueJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        recorder = HardwareCaptureRecorder(applicationContext, this)
        evidenceQueuer = CapturedEvidenceQueuer(applicationContext)
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_START -> {
                startCapture(intent)
                START_STICKY
            }

            ACTION_STOP -> {
                stopCapture()
                START_NOT_STICKY
            }

            else -> {
                // A sticky restart delivers a null intent; don't linger without a recording.
                if (activeRecording == null) stopSelf()
                START_NOT_STICKY
            }
        }
    }

    override fun onDestroy() {
        recorder.stop()
        serviceScope.cancel()
        // An error must outlive the service so the UI can tell the user why recording stopped.
        if (CaptureServiceState.state.value !is RecordingState.Error) {
            CaptureServiceState.update(RecordingState.Idle)
        }
        super.onDestroy()
    }

    private fun startCapture(intent: Intent) {
        val evidenceId = intent.getStringExtra(EXTRA_EVIDENCE_ID) ?: newEvidenceId()
        val requestedCaptureMode = parseCaptureMode(intent.getStringExtra(EXTRA_CAPTURE_MODE))
        val requestedMediaType = parseMediaType(intent.getStringExtra(EXTRA_MEDIA_TYPE))
        startForegroundCompat(createNotification())
        // Starting again would replace the recorder and lose the clip being recorded.
        if (activeRecording != null) return

        val decision = captureStartDecision(requestedCaptureMode, requestedMediaType)

        when (decision) {
            is CaptureStartDecision.Start -> startRecorder(evidenceId, decision)
            CaptureStartDecision.StopGracefully -> stopForCriticalBattery()
        }
    }

    private fun startRecorder(evidenceId: String, decision: CaptureStartDecision.Start) {
        val recording = RecordingState.Active(
            evidenceId = evidenceId,
            startedAt = Instant.now(),
            mode = decision.captureMode,
            mediaType = decision.mediaType,
            quality = CaptureQuality.DefaultVideo,
        )
        activeRecording = recording
        CaptureServiceState.update(recording)
        recorder.start(
            HardwareCaptureRequest(
                evidenceId = evidenceId,
                mediaType = decision.mediaType,
                quality = CaptureQuality.DefaultVideo,
            ),
        )
    }

    private fun stopForCriticalBattery() {
        activeRecording = null
        CaptureServiceState.update(
            RecordingState.Error(
                message = getString(R.string.capture_error_battery_too_low),
                occurredAt = Instant.now(),
            ),
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopCapture() {
        // A repeated stop while the clip is being queued would queue the same file twice.
        if (queueJob?.isActive == true) return

        val currentState = CaptureServiceState.state.value
        val recording = activeRecording ?: currentState as? RecordingState.Active
        if (recording != null) {
            CaptureServiceState.update(
                RecordingState.Stopping(
                    evidenceId = recording.evidenceId,
                    requestedAt = Instant.now(),
                ),
            )
        }
        val stopResult = recorder.stop()
        val output = stopResult.output() ?: recording?.captureOutput()
        if (recording != null && output != null) {
            queueCapturedOutput(recording, output)
        } else if (stopResult is HardwareRecorderStopResult.FinalizeFailed) {
            onCaptureError(stopResult.message)
        } else {
            finishStoppedService()
        }
    }

    override fun onCaptureStarted(output: HardwareCaptureOutput) = Unit

    override fun onCaptureError(message: String) {
        // Camera callbacks arrive on the camera thread; handle state on the main thread.
        serviceScope.launch { handleCaptureError(message) }
    }

    private fun handleCaptureError(message: String) {
        val recording = activeRecording
        val output = recorder.stop().output()
        if (recording != null && output != null && output.file.length() > 0) {
            // The camera failed mid-recording: keep and queue what was already captured.
            CaptureServiceState.update(RecordingState.Error(message = message, occurredAt = Instant.now()))
            queueCapturedOutput(recording, output)
            return
        }

        activeRecording = null
        CaptureServiceState.update(
            RecordingState.Error(
                message = message,
                occurredAt = Instant.now(),
            ),
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun queueCapturedOutput(state: RecordingState.Active, output: HardwareCaptureOutput) {
        queueJob = serviceScope.launch {
            runCatching {
                evidenceQueuer.queue(
                    CapturedEvidenceQueueRequest(
                        evidenceId = state.evidenceId,
                        outputFile = output.file,
                        mediaType = state.mediaType,
                        captureMode = state.mode,
                        startedAt = state.startedAt,
                    ),
                )
            }.onSuccess {
                activeRecording = null
                finishStoppedService()
            }.onFailure { error ->
                // Keep the error visible: resetting to Idle would hide that the clip was not queued.
                activeRecording = null
                CaptureServiceState.update(
                    RecordingState.Error(
                        message = error.message ?: getString(R.string.capture_error_queue_failed),
                        occurredAt = Instant.now(),
                    ),
                )
                stopService()
            }
        }
    }

    private fun finishStoppedService() {
        CaptureServiceState.update(RecordingState.Idle)
        stopService()
    }

    private fun stopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun HardwareRecorderStopResult.output(): HardwareCaptureOutput? {
        return when (this) {
            is HardwareRecorderStopResult.Finalized -> output
            is HardwareRecorderStopResult.FinalizeFailed -> output
            HardwareRecorderStopResult.NoActiveRecorder -> null
        }
    }

    private fun RecordingState.Active.captureOutput(): HardwareCaptureOutput? {
        val outputFile = CaptureOutputFiles.latestFor(applicationContext, evidenceId, mediaType) ?: return null
        return HardwareCaptureOutput(outputFile, mediaType)
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, foregroundServiceTypes())
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    // Android 14 rejects a camera or microphone foreground service type whose permission is missing,
    // so claim only the granted ones; the recorder then reports the missing permission instead of crashing.
    private fun foregroundServiceTypes(): Int {
        var types = 0
        if (hasPermission(this, Manifest.permission.CAMERA)) {
            types = types or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        }
        if (hasPermission(this, Manifest.permission.RECORD_AUDIO)) {
            types = types or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        }
        return types
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(getString(R.string.capture_notification_title))
            .setContentText(getString(R.string.capture_notification_text))
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.capture_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun parseCaptureMode(value: String?): CaptureMode {
        return CaptureMode.entries.firstOrNull { it.name == value } ?: CaptureMode.Standard
    }

    private fun parseMediaType(value: String?): MediaType {
        return MediaType.entries.firstOrNull { it.name == value } ?: MediaType.Video
    }

    private fun captureStartDecision(requestedMode: CaptureMode, requestedMediaType: MediaType): CaptureStartDecision {
        val batteryAction = batteryCapturePolicy.actionForBatteryPercent(currentBatteryPercent())
        return captureStartPolicy.decisionFor(
            requestedMode = requestedMode,
            requestedMediaType = requestedMediaType,
            batteryAction = batteryAction,
        )
    }

    private fun currentBatteryPercent(): Int {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (level >= 0 && scale > 0) {
            level * FULL_BATTERY_SCALE_PERCENT / scale
        } else {
            FULL_BATTERY_SCALE_PERCENT
        }
    }

    companion object {
        /**
         * Every capture mode records audio, and the service cannot become a foreground service
         * without a granted capture permission, so callers must check this before starting it.
         */
        fun hasRequiredPermissions(context: Context): Boolean {
            return hasPermission(context, Manifest.permission.RECORD_AUDIO)
        }

        fun reportMissingPermissions(context: Context) {
            CaptureServiceState.update(
                RecordingState.Error(
                    message = context.getString(R.string.capture_error_microphone_permission),
                    occurredAt = Instant.now(),
                ),
            )
        }

        private fun hasPermission(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        // Evidence IDs are shared across every device uploading to a node, so they must be
        // globally unique; timestamp-based IDs collide when two people record at once.
        fun newEvidenceId(prefix: String = "evidence"): String {
            return "$prefix-${UUID.randomUUID()}"
        }

        fun startIntent(
            context: Context,
            evidenceId: String,
            captureMode: CaptureMode = CaptureMode.Standard,
            mediaType: MediaType = MediaType.Video,
        ): Intent {
            return Intent(context, CaptureService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_EVIDENCE_ID, evidenceId)
                .putExtra(EXTRA_CAPTURE_MODE, captureMode.name)
                .putExtra(EXTRA_MEDIA_TYPE, mediaType.name)
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, CaptureService::class.java).setAction(ACTION_STOP)
        }
    }
}
