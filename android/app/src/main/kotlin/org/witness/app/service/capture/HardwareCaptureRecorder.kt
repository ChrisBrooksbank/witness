package org.witness.app.service.capture

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import org.witness.app.domain.model.CaptureQuality
import org.witness.app.domain.model.MediaType

private const val VIDEO_BITRATE = 3_000_000
private const val AUDIO_SAMPLE_RATE = 44_100
private const val CAMERA_THREAD_NAME = "WitnessCameraCapture"
private const val ORIENTATION_HINT_DEGREES = 90

@Suppress("TooManyFunctions")
class HardwareCaptureRecorder(
    private val context: Context,
    private val listener: Listener,
) {
    private var mediaRecorder: MediaRecorder? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var cameraThread: HandlerThread? = null
    private var cameraHandler: Handler? = null

    fun start(request: HardwareCaptureRequest) {
        if (!hasAudioPermission()) {
            listener.onCaptureError("Microphone permission is required before recording.")
            return
        }

        when (request.mediaType) {
            MediaType.Audio -> startAudioOnly(request)
            MediaType.Video -> startVideo(request)
            MediaType.Photo -> listener.onCaptureError("Photo capture is not supported by the MVP recorder.")
        }
    }

    fun stop() {
        stopMediaRecorder()
        closeCamera()
        stopCameraThread()
    }

    private fun startAudioOnly(request: HardwareCaptureRequest) {
        val outputFile = outputFileFor(request)
        val recorder = newMediaRecorder()
        mediaRecorder = recorder

        try {
            configureAudioRecorder(recorder, outputFile, request.quality)
            recorder.prepare()
            recorder.start()
            listener.onCaptureStarted(HardwareCaptureOutput(outputFile, MediaType.Audio))
        } catch (exception: IOException) {
            failStart("Unable to prepare audio recorder.", exception)
        } catch (exception: RuntimeException) {
            failStart("Unable to start audio recorder.", exception)
        }
    }

    private fun startVideo(request: HardwareCaptureRequest) {
        if (!hasCameraPermission()) {
            listener.onCaptureError("Camera permission is required before video recording.")
            return
        }

        val outputFile = outputFileFor(request)
        val recorder = newMediaRecorder()
        mediaRecorder = recorder

        try {
            configureVideoRecorder(recorder, outputFile, request.quality)
            recorder.prepare()
            startCameraThread()
            openCamera(request, outputFile, recorder)
        } catch (exception: IOException) {
            failStart("Unable to prepare video recorder.", exception)
        } catch (exception: RuntimeException) {
            failStart("Unable to start video recorder.", exception)
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(request: HardwareCaptureRequest, outputFile: File, recorder: MediaRecorder) {
        try {
            val cameraManager = context.getSystemService(CameraManager::class.java)
            cameraManager.openCamera(
                backCameraId(cameraManager),
                cameraStateCallback(request, outputFile, recorder),
                cameraHandler
            )
        } catch (exception: CameraAccessException) {
            failStart("Unable to access the camera.", exception)
        } catch (exception: SecurityException) {
            failStart("Camera permission was denied.", exception)
        }
    }

    private fun cameraStateCallback(
        request: HardwareCaptureRequest,
        outputFile: File,
        recorder: MediaRecorder,
    ): CameraDevice.StateCallback {
        return object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createRecordingSession(camera, recorder, request, outputFile)
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                listener.onCaptureError("Camera disconnected during recording startup.")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                listener.onCaptureError("Camera failed during recording startup: $error")
            }
        }
    }

    private fun createRecordingSession(
        camera: CameraDevice,
        recorder: MediaRecorder,
        request: HardwareCaptureRequest,
        outputFile: File,
    ) {
        val recorderSurface = recorder.surface
        val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            addTarget(recorderSurface)
            set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
        }

        camera.createCaptureSession(
            listOf(recorderSurface),
            sessionStateCallback(captureRequest, recorder, request, outputFile),
            cameraHandler,
        )
    }

    private fun sessionStateCallback(
        requestBuilder: CaptureRequest.Builder,
        recorder: MediaRecorder,
        request: HardwareCaptureRequest,
        outputFile: File,
    ): CameraCaptureSession.StateCallback {
        return object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                startRepeatingRequest(session, requestBuilder, recorder, request, outputFile)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                session.close()
                listener.onCaptureError("Camera recording session could not be configured.")
            }
        }
    }

    private fun startRepeatingRequest(
        session: CameraCaptureSession,
        requestBuilder: CaptureRequest.Builder,
        recorder: MediaRecorder,
        request: HardwareCaptureRequest,
        outputFile: File,
    ) {
        try {
            session.setRepeatingRequest(requestBuilder.build(), null, cameraHandler)
            recorder.start()
            listener.onCaptureStarted(HardwareCaptureOutput(outputFile, request.mediaType))
        } catch (exception: CameraAccessException) {
            failStart("Unable to start camera capture requests.", exception)
        } catch (exception: RuntimeException) {
            failStart("Unable to start video recorder.", exception)
        }
    }

    private fun configureVideoRecorder(recorder: MediaRecorder, outputFile: File, quality: CaptureQuality) {
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setOutputFile(outputFile.absolutePath)
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        recorder.setVideoSize(quality.width, quality.height)
        recorder.setVideoFrameRate(quality.frameRate)
        recorder.setVideoEncodingBitRate(VIDEO_BITRATE)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder.setAudioEncodingBitRate(quality.audioBitrate)
        recorder.setAudioSamplingRate(AUDIO_SAMPLE_RATE)
        recorder.setOrientationHint(ORIENTATION_HINT_DEGREES)
    }

    private fun configureAudioRecorder(recorder: MediaRecorder, outputFile: File, quality: CaptureQuality) {
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setOutputFile(outputFile.absolutePath)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder.setAudioEncodingBitRate(quality.audioBitrate)
        recorder.setAudioSamplingRate(AUDIO_SAMPLE_RATE)
    }

    private fun outputFileFor(request: HardwareCaptureRequest): File {
        return CaptureOutputFiles.create(context, request.evidenceId, request.mediaType, System.currentTimeMillis())
    }

    private fun backCameraId(cameraManager: CameraManager): String {
        val backCamera = cameraManager.cameraIdList.firstOrNull { cameraId ->
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }

        return backCamera ?: cameraManager.cameraIdList.first()
    }

    private fun startCameraThread() {
        val thread = HandlerThread(CAMERA_THREAD_NAME)
        thread.start()
        cameraThread = thread
        cameraHandler = Handler(thread.looper)
    }

    private fun stopCameraThread() {
        cameraThread?.quitSafely()
        cameraThread = null
        cameraHandler = null
    }

    private fun closeCamera() {
        captureSession?.close()
        cameraDevice?.close()
        captureSession = null
        cameraDevice = null
    }

    private fun stopMediaRecorder() {
        val recorder = mediaRecorder ?: return
        try {
            recorder.stop()
        } catch (exception: RuntimeException) {
            listener.onCaptureError(
                "Recorder stopped before media was finalized. ${exception.message.orEmpty()}".trim(),
            )
        } finally {
            recorder.reset()
            recorder.release()
            mediaRecorder = null
        }
    }

    private fun failStart(message: String, exception: Exception) {
        stop()
        listener.onCaptureError("$message ${exception.message.orEmpty()}".trim())
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun newMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    interface Listener {
        fun onCaptureStarted(output: HardwareCaptureOutput)

        fun onCaptureError(message: String)
    }
}

data class HardwareCaptureRequest(
    val evidenceId: String,
    val mediaType: MediaType,
    val quality: CaptureQuality,
)

data class HardwareCaptureOutput(
    val file: File,
    val mediaType: MediaType,
)
