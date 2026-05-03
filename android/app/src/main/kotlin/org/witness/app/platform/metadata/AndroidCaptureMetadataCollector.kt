package org.witness.app.platform.metadata

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.core.content.ContextCompat
import java.time.Instant
import org.witness.app.domain.model.CaptureMode
import org.witness.app.domain.model.DeviceInfo
import org.witness.app.domain.model.EvidenceChunkHash
import org.witness.app.domain.model.EvidenceLocation
import org.witness.app.domain.model.EvidenceMetadata
import org.witness.app.domain.model.MediaType
import org.witness.app.domain.model.TimeSource

private const val LOCATION_UNAVAILABLE_PERMISSION = "location permission unavailable"
private const val LOCATION_UNAVAILABLE_LAST_KNOWN = "last known location unavailable"
private const val ORIENTATION_0_DEGREES = 0
private const val ORIENTATION_90_DEGREES = 90
private const val ORIENTATION_180_DEGREES = 180
private const val ORIENTATION_270_DEGREES = 270

data class InitialMetadataRequest(
    val evidenceId: String,
    val merkleRoot: String,
    val chunkHashes: List<EvidenceChunkHash>,
    val mediaType: MediaType,
    val captureMode: CaptureMode,
    val capturedAt: Instant = Instant.now(),
    val networkCapturedAt: Instant? = null,
)

class AndroidCaptureMetadataCollector(
    private val context: Context,
) {
    fun collectInitialMetadata(request: InitialMetadataRequest): EvidenceMetadata {
        val locationResult = lastKnownLocation()
        return EvidenceMetadata(
            evidenceId = request.evidenceId,
            merkleRoot = request.merkleRoot,
            chunkHashes = request.chunkHashes,
            capturedAt = request.capturedAt,
            networkCapturedAt = request.networkCapturedAt,
            timeSource = if (request.networkCapturedAt == null) TimeSource.Device else TimeSource.Network,
            location = locationResult.location,
            locationUnavailableReason = locationResult.unavailableReason,
            device = deviceInfo(),
            appVersion = appVersion(),
            mediaType = request.mediaType,
            captureMode = request.captureMode,
            orientationDegrees = orientationDegrees(),
            signature = null,
        )
    }

    private fun deviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER.orEmpty(),
            model = Build.MODEL.orEmpty(),
            androidVersion = Build.VERSION.RELEASE.orEmpty(),
            fingerprint = Build.FINGERPRINT.orEmpty(),
        )
    }

    private fun appVersion(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName ?: "unknown"
    }

    private fun orientationDegrees(): Int {
        val windowManager = ContextCompat.getSystemService(context, WindowManager::class.java)
        val rotation = windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        return when (rotation) {
            Surface.ROTATION_90 -> ORIENTATION_90_DEGREES
            Surface.ROTATION_180 -> ORIENTATION_180_DEGREES
            Surface.ROTATION_270 -> ORIENTATION_270_DEGREES
            else -> ORIENTATION_0_DEGREES
        }
    }

    private fun lastKnownLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult(location = null, unavailableReason = LOCATION_UNAVAILABLE_PERMISSION)
        }

        val locationManager = ContextCompat.getSystemService(context, LocationManager::class.java)
            ?: return LocationResult(location = null, unavailableReason = LOCATION_UNAVAILABLE_LAST_KNOWN)

        val bestLocation = locationManager.getProviders(true)
            .mapNotNull { provider -> locationManager.safeLastKnownLocation(provider) }
            .minByOrNull { location: Location -> location.accuracy }

        return if (bestLocation == null) {
            LocationResult(location = null, unavailableReason = LOCATION_UNAVAILABLE_LAST_KNOWN)
        } else {
            LocationResult(location = bestLocation.toEvidenceLocation(), unavailableReason = null)
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun LocationManager.safeLastKnownLocation(provider: String): Location? {
        return runCatching { getLastKnownLocation(provider) }.getOrNull()
    }

    private fun Location.toEvidenceLocation(): EvidenceLocation {
        val altitudeValue = if (hasAltitude()) altitude else null
        return EvidenceLocation(
            latitude = latitude,
            longitude = longitude,
            altitude = altitudeValue,
            accuracyMeters = accuracy,
            provider = provider ?: "unknown",
        )
    }

    private data class LocationResult(
        val location: EvidenceLocation?,
        val unavailableReason: String?,
    )
}
