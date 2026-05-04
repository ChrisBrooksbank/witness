package org.witness.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.witness.app.data.upload.EvidenceRetentionWorker
import org.witness.app.domain.model.CaptureMode
import org.witness.app.domain.model.MediaType
import org.witness.app.domain.safety.VolumeButton
import org.witness.app.domain.safety.WitnessModeArmController
import org.witness.app.domain.safety.WitnessModeSequenceDetector
import org.witness.app.service.capture.CaptureService
import org.witness.app.ui.WitnessApp
import org.witness.app.ui.theme.WitnessTheme

private const val WITNESS_ARMED_VIBRATION_MILLIS = 60L
private const val WITNESS_STARTED_VIBRATION_MILLIS = 120L
private const val CAPTURE_PERMISSION_REQUEST = 20

class MainActivity : ComponentActivity() {
    private val sequenceDetector = WitnessModeSequenceDetector()
    private val armController = WitnessModeArmController()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EvidenceRetentionWorker.enqueuePeriodic(this)
        requestCapturePermissions()
        setContent {
            WitnessTheme {
                WitnessApp()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        var handled = false

        if (event.action == KeyEvent.ACTION_DOWN) {
            val button = volumeButtonForKeyCode(event.keyCode)

            if (button != null && armController.isArmed && button == VolumeButton.Down) {
                armController.cancel()
                vibrate(WITNESS_ARMED_VIBRATION_MILLIS)
                handled = true
            } else if (button != null && sequenceDetector.recordPress(button, event.eventTime)) {
                armWitnessMode(event.eventTime)
                handled = true
            }
        }

        return handled || super.dispatchKeyEvent(event)
    }

    private fun volumeButtonForKeyCode(keyCode: Int): VolumeButton? {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> VolumeButton.Up
            KeyEvent.KEYCODE_VOLUME_DOWN -> VolumeButton.Down
            else -> null
        }
    }

    private fun armWitnessMode(armedAtMillis: Long) {
        val state = armController.arm(armedAtMillis)
        vibrate(WITNESS_ARMED_VIBRATION_MILLIS)
        mainHandler.postDelayed(
            {
                if (armController.activationDue(System.currentTimeMillis())) {
                    startWitnessMode()
                }
            },
            state.activatesAtMillis - armedAtMillis,
        )
    }

    private fun startWitnessMode() {
        vibrate(WITNESS_STARTED_VIBRATION_MILLIS)
        val intent = CaptureService.startIntent(
            context = this,
            evidenceId = "witness-${System.currentTimeMillis()}",
            captureMode = CaptureMode.Witness,
            mediaType = MediaType.Video,
        )
        ContextCompat.startForegroundService(this, intent)
    }

    private fun vibrate(durationMillis: Long) {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val manager = getSystemService(VibratorManager::class.java)
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun requestCapturePermissions() {
        val missingPermissions = capturePermissions().filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                CAPTURE_PERMISSION_REQUEST,
            )
        }
    }

    private fun capturePermissions(): List<String> {
        return listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }
}
