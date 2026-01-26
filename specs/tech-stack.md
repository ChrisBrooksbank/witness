# Tech Stack Research: Witness MVP

**Version:** 2.0
**Date:** 2026-01-26
**Status:** Decision Made — Android-Only MVP

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Decision: Android-Only MVP](#decision-android-only-mvp)
3. [Technical Requirements](#technical-requirements)
4. [Chosen Stack: Native Android + Go](#chosen-stack-native-android--go)
5. [Architecture](#architecture)
6. [Key Implementation Details](#key-implementation-details)
7. [Distribution Strategy](#distribution-strategy)
8. [Future iOS Path](#future-ios-path)
9. [Appendix: Cross-Platform Options Considered](#appendix-cross-platform-options-considered)

---

## Executive Summary

After analyzing requirements and practical constraints, **Witness MVP will be Android-only using native Kotlin + Jetpack Compose**, with a Go backend for federation.

### Decision Rationale

| Factor | Impact |
|--------|--------|
| **Available devices** | Development team has Android/Pixel devices only |
| **Apple Developer credentials** | Not available; iOS development blocked |
| **Witness mode (screen-off recording)** | Works fully on Android; iOS cannot do this |
| **Camouflage** | Activity aliases work perfectly on Android; iOS has App Store restrictions |
| **Distribution** | APK sideloading + F-Droid avoids app store approval risks |
| **Target demographic** | Android dominates in immigrant communities (more affordable devices) |

### Chosen Stack

```
┌─────────────────────────────────────────────────────┐
│  ANDROID CLIENT        │  FEDERATION BACKEND        │
├─────────────────────────────────────────────────────┤
│  Kotlin + Jetpack      │  Go + PostgreSQL +         │
│  Compose               │  MinIO + Redis             │
└─────────────────────────────────────────────────────┘
```

---

## Decision: Android-Only MVP

### Why Android-Only

| Advantage | Explanation |
|-----------|-------------|
| **Full witness mode** | Foreground Service + WakeLock enables true screen-off video recording |
| **Complete camouflage** | Activity aliases allow full icon and app name changes |
| **Simpler development** | Single platform, no cross-platform abstraction overhead |
| **Faster MVP** | No iOS work means faster time to usable product |
| **Sideload distribution** | APK installation without app store approval |
| **F-Droid option** | Trusted FOSS distribution channel |
| **Target users** | Android has higher market share among immigrant communities |
| **No Apple fees** | Avoids $99/year Apple Developer Program |

### What We Gain vs Cross-Platform

| Capability | Cross-Platform Approach | Android-Native Approach |
|------------|------------------------|------------------------|
| Camera control | Plugin + bridge | Direct Camera2/CameraX API |
| Background recording | Native module required anyway | Direct ForegroundService |
| Video encoding | Native module required anyway | Direct MediaCodec |
| Camouflage | Native module required anyway | Direct Activity aliases |
| Bluetooth mesh | Plugin + native fallback | Direct Android BLE |
| App size | 15-30 MB (Flutter) | 8-12 MB |
| Startup time | Good | Best |
| Debugging | Good | Best |

### Trade-offs Accepted

| Trade-off | Mitigation |
|-----------|------------|
| No iOS users | Android is ~70% of global market; revisit for v2 |
| Smaller addressable market | Focus on highest-need users first |
| Future iOS effort | Clean architecture allows later KMP extraction or native port |

---

## Technical Requirements

### Critical Android Features

All core Witness features map directly to Android platform capabilities:

```
WITNESS FEATURE                 ANDROID IMPLEMENTATION
─────────────────────────────────────────────────────────
Witness mode (screen-off)   →   ForegroundService + WakeLock + Camera2
Camouflage (icon/name)      →   Activity aliases in AndroidManifest
Decoy mode                  →   Separate launcher Activity
Volume button trigger       →   MediaSession or AccessibilityService
Video capture               →   Camera2 API + CameraX
Hardware encoding           →   MediaCodec (H.264/H.265)
Live streaming              →   RTMP/HLS via MediaMuxer
Encrypted storage           →   EncryptedSharedPreferences + SQLCipher
Upload queue                →   WorkManager (survives reboot)
Bluetooth mesh              →   Android BLE APIs
GPS metadata                →   FusedLocationProvider
Hash at capture             →   MessageDigest (SHA-256)
```

### Minimum Android Version

| Target | Version | API Level | Rationale |
|--------|---------|-----------|-----------|
| **Minimum** | Android 8.0 (Oreo) | API 26 | ForegroundService behavior, ~95% device coverage |
| **Target** | Android 14 | API 34 | Latest features, Pixel optimization |
| **Compile** | Android 14 | API 34 | Access to newest APIs |

### Permissions Required

```xml
<!-- Camera and recording -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- Background operation -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Location for metadata -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Networking -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Bluetooth mesh -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Boot persistence for upload queue -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

---

## Chosen Stack: Native Android + Go

### Android Client Stack

| Layer | Technology | Version | Rationale |
|-------|------------|---------|-----------|
| **Language** | Kotlin | 2.0+ | Modern, null-safe, coroutines |
| **UI** | Jetpack Compose | 1.6+ | Declarative, modern, Google-recommended |
| **Architecture** | MVVM + Clean Architecture | — | Testable, maintainable, scalable |
| **DI** | Hilt | 2.50+ | Official Jetpack DI, compile-time safe |
| **Async** | Coroutines + Flow | 1.8+ | Structured concurrency, reactive streams |
| **Networking** | Retrofit + OkHttp | 2.9+ / 4.12+ | Industry standard, interceptors |
| **Database** | Room | 2.6+ | Type-safe, reactive, migration support |
| **Encrypted Storage** | EncryptedSharedPreferences + SQLCipher | — | Platform-backed encryption |
| **Camera** | Camera2 + CameraX | 1.3+ | Full control + simplified API |
| **Video Encoding** | MediaCodec | — | Hardware H.264/H.265 |
| **Background** | WorkManager + ForegroundService | 2.9+ | Guaranteed execution, survives reboot |
| **Bluetooth** | Android BLE APIs | — | Direct platform access |
| **Crypto** | Tink + Android Keystore | 1.12+ | Google's crypto library |
| **Image Loading** | Coil | 2.5+ | Kotlin-first, Compose integration |

### Go Backend Stack

| Component | Technology | Version | Rationale |
|-----------|------------|---------|-----------|
| **Language** | Go | 1.22+ | Single binary, efficient, cross-compile |
| **HTTP Framework** | Gin | 1.9+ | Fast, lightweight, middleware support |
| **Database** | PostgreSQL | 16+ | JSONB, reliability, ACID |
| **DB Driver** | pgx | 5.5+ | Native Go driver, best performance |
| **Object Storage** | MinIO (S3 API) | — | Self-hostable, standard API |
| **Queue** | Redis Streams | 7+ | Lightweight, persistent, pub/sub |
| **Auth** | JWT | — | Stateless, anonymous-compatible |
| **Crypto** | golang.org/x/crypto | — | Standard library quality |
| **Migrations** | golang-migrate | 4.17+ | Version-controlled schema |

---

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         WITNESS ARCHITECTURE                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  ANDROID CLIENT                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                                                                          │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐   │ │
│  │  │                    UI LAYER (Jetpack Compose)                    │   │ │
│  │  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │   │ │
│  │  │  │  Main UI     │  │  Camouflage  │  │   Decoy UI   │          │   │ │
│  │  │  │  (Record)    │  │  (Calculator)│  │   (Fake App) │          │   │ │
│  │  │  └──────────────┘  └──────────────┘  └──────────────┘          │   │ │
│  │  └─────────────────────────────────────────────────────────────────┘   │ │
│  │                                    │                                    │ │
│  │  ┌─────────────────────────────────┴─────────────────────────────────┐ │ │
│  │  │                    DOMAIN LAYER (ViewModels)                       │ │ │
│  │  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │ │ │
│  │  │  │  Recording   │  │   Upload     │  │   Settings   │            │ │ │
│  │  │  │  ViewModel   │  │  ViewModel   │  │  ViewModel   │            │ │ │
│  │  │  └──────────────┘  └──────────────┘  └──────────────┘            │ │ │
│  │  └───────────────────────────────────────────────────────────────────┘ │ │
│  │                                    │                                    │ │
│  │  ┌─────────────────────────────────┴─────────────────────────────────┐ │ │
│  │  │                    DATA LAYER (Repositories)                       │ │ │
│  │  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │ │ │
│  │  │  │  Evidence    │  │   Upload     │  │    Node      │            │ │ │
│  │  │  │  Repository  │  │  Repository  │  │  Repository  │            │ │ │
│  │  │  └──────────────┘  └──────────────┘  └──────────────┘            │ │ │
│  │  └───────────────────────────────────────────────────────────────────┘ │ │
│  │                                    │                                    │ │
│  │  ┌─────────────────────────────────┴─────────────────────────────────┐ │ │
│  │  │                    PLATFORM SERVICES                               │ │ │
│  │  │  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐     │ │ │
│  │  │  │  Capture   │ │   Upload   │ │  Crypto    │ │ Bluetooth  │     │ │ │
│  │  │  │  Service   │ │  Worker    │ │  Manager   │ │   Mesh     │     │ │ │
│  │  │  │ (Camera2 + │ │(WorkManager│ │  (Tink +   │ │  (BLE)     │     │ │ │
│  │  │  │ MediaCodec)│ │ + Retrofit)│ │ Keystore)  │ │            │     │ │ │
│  │  │  └────────────┘ └────────────┘ └────────────┘ └────────────┘     │ │ │
│  │  └───────────────────────────────────────────────────────────────────┘ │ │
│  │                                    │                                    │ │
│  │  ┌─────────────────────────────────┴─────────────────────────────────┐ │ │
│  │  │                    LOCAL STORAGE                                   │ │ │
│  │  │  ┌──────────────────────┐  ┌──────────────────────┐              │ │ │
│  │  │  │  Room Database       │  │  Encrypted Files     │              │ │ │
│  │  │  │  (metadata, queue)   │  │  (video chunks)      │              │ │ │
│  │  │  └──────────────────────┘  └──────────────────────┘              │ │ │
│  │  └───────────────────────────────────────────────────────────────────┘ │ │
│  │                                                                          │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
│                                      │                                        │
│                                HTTPS / WebSocket                              │
│                                      │                                        │
│  FEDERATION BACKEND                  │                                        │
│  ┌───────────────────────────────────┴───────────────────────────────────┐   │
│  │                            Go Server                                   │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │   │
│  │  │   REST API  │  │   Upload    │  │  Federation │  │  Streaming  │  │   │
│  │  │  (Gin)      │  │   Handler   │  │    Sync     │  │   (WebRTC)  │  │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  │   │
│  │         │                │                │                │          │   │
│  │  ┌──────┴────────────────┴────────────────┴────────────────┴──────┐  │   │
│  │  │                      Storage Layer                              │  │   │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐                      │  │   │
│  │  │  │PostgreSQL│  │  MinIO   │  │  Redis   │                      │  │   │
│  │  │  │(metadata)│  │ (videos) │  │ (queue)  │                      │  │   │
│  │  │  └──────────┘  └──────────┘  └──────────┘                      │  │   │
│  │  └─────────────────────────────────────────────────────────────────┘  │   │
│  └───────────────────────────────────────────────────────────────────────┘   │
│                                      │                                        │
│                              Federation Sync                                  │
│                                      │                                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐           │
│  │   Node A (ACLU)  │◄─┤   Node B (Org)   │◄─┤  Node C (Self)   │           │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘           │
│                                                                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Data Flow

```
CAPTURE FLOW:
┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐
│ Camera │───▶│Encoder │───▶│ Hash   │───▶│Encrypt │───▶│ Queue  │
│(Camera2)    │(MediaCodec)  │(SHA-256)    │(Tink)      │(Room)  │
└────────┘    └────────┘    └────────┘    └────────┘    └────────┘
                                                              │
UPLOAD FLOW:                                                  │
┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐         │
│ Worker │◀───│ Chunk  │◀───│  TLS   │◀───│  Node  │◀────────┘
│(WorkMgr)    │ Upload │    │ 1.3    │    │  API   │
└────────┘    └────────┘    └────────┘    └────────┘
                                               │
FEDERATION FLOW:                               │
┌────────┐    ┌────────┐    ┌────────┐        │
│ Node A │◀──▶│ Node B │◀──▶│ Node C │◀───────┘
│        │    │        │    │        │
└────────┘    └────────┘    └────────┘
```

---

## Key Implementation Details

### Witness Mode (Covert Recording)

The core differentiating feature — recording with screen off:

```kotlin
class WitnessModeService : Service() {

    private lateinit var cameraManager: CameraManager
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start as foreground service (required for Android 8+)
        startForeground(NOTIFICATION_ID, createSilentNotification())

        // Initialize camera and start recording
        initializeCamera()
        startRecording()

        return START_STICKY // Restart if killed
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Witness::WitnessMode"
        )
        wakeLock.acquire(TimeUnit.HOURS.toMillis(4)) // Max recording time
    }

    private fun createSilentNotification(): Notification {
        // Create minimal notification (required for foreground service)
        // Can be disguised as system notification
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("") // Empty or camouflaged
            .setSmallIcon(R.drawable.ic_transparent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun startRecording() {
        // Camera2 API setup for background recording
        // MediaCodec for hardware H.264 encoding
        // Output to encrypted file chunks
    }
}
```

### Camouflage Implementation

Activity aliases enable complete icon/name change:

```xml
<!-- AndroidManifest.xml -->

<!-- Real app entry (disabled by default) -->
<activity
    android:name=".ui.main.MainActivity"
    android:exported="true"
    android:enabled="false">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<!-- Calculator disguise (enabled by default) -->
<activity-alias
    android:name=".CalculatorLauncher"
    android:icon="@mipmap/ic_calculator"
    android:label="Calculator"
    android:targetActivity=".ui.camouflage.CamouflageActivity"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity-alias>

<!-- Weather disguise (disabled by default) -->
<activity-alias
    android:name=".WeatherLauncher"
    android:icon="@mipmap/ic_weather"
    android:label="Weather"
    android:targetActivity=".ui.camouflage.CamouflageActivity"
    android:enabled="false"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity-alias>

<!-- Notes disguise -->
<activity-alias
    android:name=".NotesLauncher"
    android:icon="@mipmap/ic_notes"
    android:label="Notes"
    android:targetActivity=".ui.camouflage.CamouflageActivity"
    android:enabled="false"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity-alias>
```

Switching camouflage at runtime:

```kotlin
object CamouflageManager {

    enum class Disguise(val aliasName: String) {
        CALCULATOR(".CalculatorLauncher"),
        WEATHER(".WeatherLauncher"),
        NOTES(".NotesLauncher"),
        NONE(".ui.main.MainActivity")
    }

    fun switchDisguise(context: Context, newDisguise: Disguise) {
        val packageManager = context.packageManager
        val packageName = context.packageName

        // Disable all aliases
        Disguise.entries.forEach { disguise ->
            packageManager.setComponentEnabledSetting(
                ComponentName(packageName, "$packageName${disguise.aliasName}"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        // Enable selected alias
        packageManager.setComponentEnabledSetting(
            ComponentName(packageName, "$packageName${newDisguise.aliasName}"),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
```

### Volume Button Trigger

Activate witness mode via volume button sequence:

```kotlin
class VolumeButtonReceiver : BroadcastReceiver() {

    private val pattern = listOf(
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_DOWN
    )
    private val pressSequence = mutableListOf<Int>()
    private var lastPressTime = 0L

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MEDIA_BUTTON) return

        val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
        if (event?.action != KeyEvent.ACTION_DOWN) return

        val currentTime = System.currentTimeMillis()

        // Reset if too slow (> 500ms between presses)
        if (currentTime - lastPressTime > 500) {
            pressSequence.clear()
        }

        pressSequence.add(event.keyCode)
        lastPressTime = currentTime

        // Check if pattern matches
        if (pressSequence.takeLast(pattern.size) == pattern) {
            pressSequence.clear()
            triggerWitnessMode(context)
        }
    }

    private fun triggerWitnessMode(context: Context) {
        // Haptic feedback (subtle confirmation)
        val vibrator = context.getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))

        // Start witness mode service
        val intent = Intent(context, WitnessModeService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
}
```

### Persistent Upload Queue

WorkManager ensures uploads survive app/device restarts:

```kotlin
class UploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject lateinit var uploadRepository: UploadRepository
    @Inject lateinit var nodeApi: NodeApi

    override suspend fun doWork(): Result {
        val evidenceId = inputData.getString("evidence_id") ?: return Result.failure()

        return try {
            val evidence = uploadRepository.getEvidence(evidenceId)

            // Upload in chunks
            evidence.chunks.forEach { chunk ->
                if (!chunk.uploaded) {
                    nodeApi.uploadChunk(chunk)
                    uploadRepository.markChunkUploaded(chunk.id)
                }
            }

            // Verify with hash
            nodeApi.verifyHash(evidence.hash)

            // Delete local copy after confirmation
            uploadRepository.deleteLocalEvidence(evidenceId)

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 10) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        fun enqueue(context: Context, evidenceId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<UploadWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf("evidence_id" to evidenceId))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "upload_$evidenceId",
                    ExistingWorkPolicy.KEEP,
                    request
                )
        }
    }
}
```

### Evidence Hashing

SHA-256 hash at moment of capture:

```kotlin
class EvidenceHasher {

    fun hashVideoChunk(chunk: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(chunk)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashVideoFile(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun createMetadataBundle(
        videoHash: String,
        location: Location?,
        timestamp: Long,
        deviceInfo: DeviceInfo
    ): EvidenceMetadata {
        return EvidenceMetadata(
            videoHash = videoHash,
            captureTimestamp = timestamp,
            latitude = location?.latitude,
            longitude = location?.longitude,
            locationAccuracy = location?.accuracy,
            deviceManufacturer = deviceInfo.manufacturer,
            deviceModel = deviceInfo.model,
            androidVersion = deviceInfo.androidVersion,
            appVersion = deviceInfo.appVersion
        )
    }
}
```

---

## Distribution Strategy

### Primary Channels

| Channel | Priority | Pros | Cons |
|---------|----------|------|------|
| **GitHub Releases** | P1 | Immediate updates, developer-friendly, versioned | Manual install |
| **F-Droid** | P1 | Trusted FOSS channel, auto-updates, discoverable | Slower review |
| **Direct APK** | P2 | Full control, no gatekeepers | No auto-update |
| **Obtainium** | P3 | Auto-updates from GitHub | Requires app install |

### F-Droid Preparation

Requirements for F-Droid inclusion:
- Fully open source (GPL/Apache/MIT)
- No proprietary dependencies
- Reproducible builds
- No tracking/analytics

```yaml
# F-Droid metadata (fdroid/metadata/org.witness.app.yml)
Categories:
  - Security
  - Multimedia
License: GPL-3.0-only
AuthorName: Witness Project
SourceCode: https://github.com/witness-project/witness-android
IssueTracker: https://github.com/witness-project/witness-android/issues

AutoName: Witness

Description: |
  Secure video evidence app for citizen journalists.

  Features:
  * Covert recording with screen off
  * App camouflage (disguise as calculator)
  * Encrypted storage
  * Federated backup to trusted organizations
  * Anonymous accounts

RepoType: git
Repo: https://github.com/witness-project/witness-android.git

Builds:
  - versionName: 1.0.0
    versionCode: 1
    commit: v1.0.0
    subdir: app
    gradle:
      - yes
```

### Avoiding Play Store (Initially)

Reasons to avoid Google Play for MVP:
1. **Content policy risk** — App may be flagged for "facilitating illegal activity"
2. **Approval delays** — Review process unpredictable
3. **Update control** — Google can pull app at any time
4. **Identity requirements** — Developer account requires identity

Future consideration: Apply to Play Store once app is established and has legal backing.

---

## Future iOS Path

If iOS becomes necessary, options:

### Option 1: Kotlin Multiplatform (Recommended)

Extract business logic to shared KMP module:

```
┌─────────────────────────────────────────────────────────────┐
│                    FUTURE KMP ARCHITECTURE                   │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  SHARED (Kotlin Multiplatform)                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  • Upload Manager                                       │ │
│  │  • Encryption Logic                                     │ │
│  │  • Federation Protocol                                  │ │
│  │  • Evidence Hashing                                     │ │
│  │  • Data Models                                          │ │
│  └────────────────────────────────────────────────────────┘ │
│         │                                   │                │
│         ▼                                   ▼                │
│  ┌──────────────────┐           ┌──────────────────┐        │
│  │  Android App     │           │    iOS App       │        │
│  │  (Existing)      │           │    (New Swift)   │        │
│  │  Jetpack Compose │           │    SwiftUI       │        │
│  └──────────────────┘           └──────────────────┘        │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Option 2: Native Swift Port

Separate iOS codebase with shared:
- API specification
- Federation protocol
- Design system

### iOS Limitations to Accept

| Feature | Android | iOS Possible? |
|---------|---------|---------------|
| Screen-off video recording | Yes | **No** |
| Full camouflage | Yes | Limited (icon only) |
| Sideload distribution | Yes | TestFlight only (requires account) |
| Background video | Yes | **No** (audio only) |
| Volume button trigger | Yes | Limited |

**Recommendation:** If iOS is needed, focus on audio recording + location tracking for "witness mode" rather than trying to replicate Android's video capability.

---

## Appendix: Cross-Platform Options Considered

The following options were evaluated before deciding on Android-only native:

### Flutter + Go

**Considered but rejected** because:
- Still requires native Kotlin modules for camera/background
- Cross-platform benefit lost with Android-only
- Adds Dart learning curve and Flutter overhead

### Kotlin Multiplatform

**Deferred to future** because:
- Valuable for iOS expansion later
- Overkill for Android-only MVP
- Added complexity for single platform

### .NET MAUI

**Not recommended** because:
- Weaker mobile media ecosystem
- No precedent in human rights/activist space
- Larger app size
- Desktop heritage doesn't fit mobile-only MVP

---

## Development Dependencies

### Android Project Setup

```kotlin
// build.gradle.kts (app module)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.witness.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.witness.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.video)

    // WorkManager
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)

    // Crypto
    implementation(libs.tink)
    implementation(libs.security.crypto)

    // Coroutines
    implementation(libs.coroutines.android)
}
```

### Go Backend Setup

```go
// go.mod
module github.com/witness-project/witness-node

go 1.22

require (
    github.com/gin-gonic/gin v1.9.1
    github.com/jackc/pgx/v5 v5.5.1
    github.com/minio/minio-go/v7 v7.0.66
    github.com/redis/go-redis/v9 v9.4.0
    github.com/golang-jwt/jwt/v5 v5.2.0
    github.com/golang-migrate/migrate/v4 v4.17.0
    golang.org/x/crypto v0.18.0
    google.golang.org/protobuf v1.32.0
)
```

---

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-25 | Initial research (cross-platform options) |
| 2.0 | 2026-01-26 | Decision: Android-only MVP with native Kotlin |

---

*This document reflects the current technical direction. Architecture may evolve as development progresses.*
