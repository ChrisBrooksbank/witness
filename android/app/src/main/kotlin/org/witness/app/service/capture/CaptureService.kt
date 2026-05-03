package org.witness.app.service.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.time.Instant
import org.witness.app.R
import org.witness.app.domain.model.CaptureMode
import org.witness.app.domain.model.CaptureQuality
import org.witness.app.domain.model.MediaType
import org.witness.app.domain.model.RecordingState

private const val ACTION_START = "org.witness.app.action.START_CAPTURE"
private const val ACTION_STOP = "org.witness.app.action.STOP_CAPTURE"
private const val EXTRA_EVIDENCE_ID = "evidence_id"
private const val NOTIFICATION_ID = 1001
private const val CHANNEL_ID = "capture_status"

class CaptureService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
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

            else -> START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        CaptureServiceState.update(RecordingState.Idle)
        super.onDestroy()
    }

    private fun startCapture(intent: Intent) {
        val evidenceId = intent.getStringExtra(EXTRA_EVIDENCE_ID) ?: newEvidenceId()
        CaptureServiceState.update(
            RecordingState.Active(
                evidenceId = evidenceId,
                startedAt = Instant.now(),
                mode = CaptureMode.Standard,
                mediaType = MediaType.Video,
                quality = CaptureQuality.DefaultVideo,
            ),
        )
        startForegroundCompat(createNotification())
    }

    private fun stopCapture() {
        val currentState = CaptureServiceState.state.value
        if (currentState is RecordingState.Active) {
            CaptureServiceState.update(
                RecordingState.Stopping(
                    evidenceId = currentState.evidenceId,
                    requestedAt = Instant.now(),
                ),
            )
        }
        CaptureServiceState.update(RecordingState.Idle)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
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

    private fun newEvidenceId(): String {
        return "evidence-${System.currentTimeMillis()}"
    }

    companion object {
        fun startIntent(context: Context, evidenceId: String): Intent {
            return Intent(context, CaptureService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_EVIDENCE_ID, evidenceId)
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, CaptureService::class.java).setAction(ACTION_STOP)
        }
    }
}
