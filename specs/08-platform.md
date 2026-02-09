# Platform Requirements

## Overview

Witness targets Android for MVP, with specific performance, reliability, and compatibility requirements. The app must work on affordable devices common in target communities.

## Requirements

### Android Platform

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| PLT-001 | Minimum SDK: Android 10 (API 29) | Must | Yes |
| PLT-002 | Target SDK: Android 14 (API 34) | Must | Yes |
| PLT-003 | Support devices with 2GB RAM | Must | Yes |
| PLT-004 | Support devices with limited storage | Must | Yes |
| PLT-005 | Work without Google Play Services | Should | Yes |

### Performance

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| PLT-010 | App launch to recording: < 3 seconds | Must | Yes |
| PLT-011 | Witness mode activation: < 2 seconds | Must | Yes |
| PLT-012 | First upload chunk: < 10 seconds on good connectivity | Must | Yes |
| PLT-013 | Battery impact comparable to native camera | Should | Yes |
| PLT-014 | Memory usage < 150MB during recording | Should | Yes |
| PLT-015 | Smooth 30fps camera preview | Must | Yes |

### App Size

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| PLT-020 | APK size < 15MB | Must | Yes |
| PLT-021 | App install size < 50MB | Must | Yes |
| PLT-022 | No large embedded assets | Must | Yes |
| PLT-023 | Proguard/R8 minification enabled | Must | Yes |

### Reliability

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| PLT-030 | App must not crash during recording | Must | Yes |
| PLT-031 | Must survive backgrounding | Must | Yes |
| PLT-032 | Must survive phone calls | Must | Yes |
| PLT-033 | Must survive low memory conditions | Must | Yes |
| PLT-034 | Graceful degradation, never silent failure | Must | Yes |
| PLT-035 | Queued uploads persist across restarts | Must | Yes |

### Permissions

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| PLT-040 | Request only necessary permissions | Must | Yes |
| PLT-041 | Explain permissions before requesting | Should | Yes |
| PLT-042 | Function with partial permissions (degrade gracefully) | Should | Yes |
| PLT-043 | No background location unless recording | Must | Yes |

## Acceptance Criteria

- [ ] AC-PLT-1: App installs on Android 10 device
- [ ] AC-PLT-2: App launches in under 3 seconds on mid-range device
- [ ] AC-PLT-3: Recording starts within 2 seconds of button tap
- [ ] AC-PLT-4: APK size is under 15MB
- [ ] AC-PLT-5: App does not crash during 1-hour continuous recording test
- [ ] AC-PLT-6: Queued uploads remain after force-stop and restart
- [ ] AC-PLT-7: App functions on device with 2GB RAM
- [ ] AC-PLT-8: App functions without Google Play Services

## Technical Notes

### Build Configuration

```kotlin
// build.gradle.kts (app)
android {
    namespace = "org.witness.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.witness.app"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### Required Permissions

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

<!-- Boot persistence for upload queue -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

### Performance Monitoring

```kotlin
object PerformanceMonitor {
    fun measureStartupTime(): Long {
        val startTime = SystemClock.elapsedRealtime()
        // ... app initialization
        return SystemClock.elapsedRealtime() - startTime
    }

    fun measureRecordingStart(): Long {
        val startTime = SystemClock.elapsedRealtime()
        // ... start recording
        return SystemClock.elapsedRealtime() - startTime
    }

    fun checkMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
}
```

### Low Memory Handling

```kotlin
class WitnessApplication : Application() {
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_RUNNING_LOW -> {
                // Clear caches, reduce quality
                ImageLoader.clearCache()
            }
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // Aggressive memory reduction
                // But NEVER interrupt active recording
            }
        }
    }
}
```

### Device Compatibility

Test on these reference devices:

| Device Type | Example | RAM | Storage |
|-------------|---------|-----|---------|
| Budget | Samsung Galaxy A03 | 3GB | 32GB |
| Mid-range | Google Pixel 4a | 6GB | 128GB |
| Older flagship | Samsung Galaxy S9 | 4GB | 64GB |
| Budget (older) | Motorola Moto G7 | 2GB | 32GB |

### Battery Optimization

```kotlin
class BatteryOptimizer(private val context: Context) {

    fun isOptimizationDisabled(): Boolean {
        val pm = context.getSystemService(PowerManager::class.java)
        return pm?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    fun requestDisableOptimization() {
        // Prompt user to disable battery optimization
        // Required for reliable background recording
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    }
}
```

### Distribution

| Channel | Priority | Notes |
|---------|----------|-------|
| GitHub Releases | P0 | Primary, immediate updates |
| F-Droid | P1 | FOSS community, auto-updates |
| Direct APK | P2 | Fallback, manual updates |

## Open Questions

| Question | Status |
|----------|--------|
| Google Play Store submission? | Defer - risk of removal |
| iOS version? | Post-MVP - limited features possible |
| Android TV / ChromeOS? | Not targeted |
| Wear OS companion? | Not planned |
