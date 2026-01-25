# Tech Stack Research: Witness MVP

**Version:** 1.0
**Date:** 2026-01-25
**Status:** Research Complete

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Technical Requirements Analysis](#technical-requirements-analysis)
3. [Option 1: Flutter + Go Federation](#option-1-flutter--go-federation)
4. [Option 2: Kotlin Multiplatform + Swift UI](#option-2-kotlin-multiplatform--swift-ui)
5. [Option 3: Native Dual-Platform](#option-3-native-dual-platform-kotlin--swift)
6. [Comparison Matrix](#comparison-matrix)
7. [Recommendation](#recommendation)
8. [Appendix: Technology Notes](#appendix-technology-notes)

---

## Executive Summary

After analyzing the Witness MVP requirements against available technology options, this document evaluates **three viable tech stack approaches**. The primary constraints driving this analysis are:

| Constraint | Impact on Stack Choice |
|------------|------------------------|
| Covert recording (screen-off) | Requires deep platform API access |
| Real-time streaming | Requires efficient media pipelines |
| App camouflage | Requires platform-specific UI tricks |
| Federation protocol | Backend must be resilient, self-hostable |
| Battery efficiency | Native code preferred for media |
| < 3 second launch-to-record | Performance critical |

### Quick Recommendation

**Primary Recommendation: Flutter + Go Federation**
- Best balance of development velocity, cross-platform coverage, and native capability
- Flutter's platform channels enable necessary native features
- Go backend is portable, efficient, and self-hostable

**Alternative for Maximum Platform Integration: Kotlin Multiplatform + SwiftUI**
- Better native API access but higher complexity
- Recommended if Flutter proves insufficient for covert recording

---

## Technical Requirements Analysis

### Critical Mobile Features

| Feature | Technical Requirement | Platform Challenge |
|---------|----------------------|-------------------|
| Screen-off recording | Background service + wake locks | Android: WorkManager + Foreground Service; iOS: Background modes + limited |
| Camouflage | Dynamic icon/name, decoy UI | Android: Activity aliases; iOS: App Store restrictions |
| Live streaming | HLS/RTMP/WebRTC streaming | Both: Native media codecs required |
| Encryption | AES-256 at rest, TLS in transit | Both: Platform crypto APIs |
| Bluetooth mesh | BLE scanning/advertising | Both: Bluetooth LE APIs |
| Hash verification | SHA-256 at capture | Both: Native crypto |
| GPS metadata | Location services | Both: Location APIs |

### Android-Specific Requirements

```
CRITICAL ANDROID FEATURES:
├── Foreground Service (required for background recording)
├── Wake Lock (keep CPU alive with screen off)
├── Camera2 API (full camera control)
├── MediaCodec (hardware encoding)
├── Activity Aliases (icon camouflage)
├── WorkManager (persistent upload queue)
└── Bluetooth LE (mesh networking)
```

### iOS-Specific Requirements

```
CRITICAL iOS FEATURES:
├── Background Modes (audio, location, processing)
├── AVFoundation (camera/audio capture)
├── VideoToolbox (hardware encoding)
├── App Groups (data sharing)
├── Background URLSession (upload queue)
├── Core Bluetooth (mesh networking)
└── CryptoKit (encryption)
```

### Federation Backend Requirements

| Requirement | Technical Implication |
|-------------|----------------------|
| Self-hostable | Single binary or Docker, minimal dependencies |
| Node-to-node replication | Event-based sync protocol |
| End-to-end encryption | Client-side encryption, server never sees plaintext |
| Horizontal scaling | Stateless API servers, shared storage |
| Resilience | Database clustering, queue persistence |

---

## Option 1: Flutter + Go Federation

### Overview

Flutter provides cross-platform UI with native performance, while Go powers a lightweight, efficient federation backend. Platform channels bridge to native code for camera, encryption, and background services.

### Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           FLUTTER + GO STACK                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  MOBILE CLIENT                                                           │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                         Flutter (Dart)                             │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │  │
│  │  │     UI      │  │   State     │  │  Business   │               │  │
│  │  │   Layer     │  │  Management │  │   Logic     │               │  │
│  │  │  (Widgets)  │  │   (Riverpod)│  │             │               │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘               │  │
│  │         │                │                │                        │  │
│  │         └────────────────┼────────────────┘                        │  │
│  │                          │                                          │  │
│  │                  Platform Channels                                  │  │
│  │                          │                                          │  │
│  │  ┌───────────────────────┴───────────────────────┐                │  │
│  │  │              Native Modules                    │                │  │
│  │  │  ┌──────────────────┐  ┌──────────────────┐  │                │  │
│  │  │  │  Android (Kotlin)│  │    iOS (Swift)   │  │                │  │
│  │  │  │  • Camera2       │  │  • AVFoundation  │  │                │  │
│  │  │  │  • MediaCodec    │  │  • VideoToolbox  │  │                │  │
│  │  │  │  • ForegroundSvc │  │  • Background    │  │                │  │
│  │  │  │  • Bluetooth LE  │  │  • CoreBluetooth │  │                │  │
│  │  │  └──────────────────┘  └──────────────────┘  │                │  │
│  │  └────────────────────────────────────────────────┘                │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                    │                                     │
│                              HTTPS/WebSocket                             │
│                                    │                                     │
│  FEDERATION BACKEND                │                                     │
│  ┌─────────────────────────────────┴─────────────────────────────────┐  │
│  │                            Go Server                               │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │  │
│  │  │   REST/gRPC │  │   Upload    │  │  Federation │               │  │
│  │  │     API     │  │   Handler   │  │   Sync      │               │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘               │  │
│  │         │                │                │                        │  │
│  │  ┌──────┴────────────────┴────────────────┴──────┐               │  │
│  │  │              Storage Layer                     │               │  │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐    │               │  │
│  │  │  │PostgreSQL│  │   S3/    │  │  Redis   │    │               │  │
│  │  │  │(metadata)│  │  MinIO   │  │ (queue)  │    │               │  │
│  │  │  └──────────┘  └──────────┘  └──────────┘    │               │  │
│  │  └────────────────────────────────────────────────┘               │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Mobile Stack Details

| Layer | Technology | Rationale |
|-------|------------|-----------|
| UI Framework | Flutter 3.x | Hot reload, single codebase, native performance |
| Language | Dart 3.x | Null safety, async/await, strong typing |
| State Management | Riverpod 2.x | Compile-safe, testable, reactive |
| Local Storage | Drift (SQLite) | Type-safe, reactive, encrypted via SQLCipher |
| Networking | Dio + web_socket_channel | HTTP client with interceptors, WebSocket support |
| Encryption | pointycastle + platform crypto | Cross-platform crypto, native for performance |
| Camera | camera plugin + platform channels | Basic camera + native extensions |
| Background | flutter_background_service + native | Cross-platform with native fallback |

### Native Module Requirements (Flutter Platform Channels)

```dart
// Platform channel interface example
class WitnessPlatform {
  static const channel = MethodChannel('witness/capture');

  // Android: Uses Camera2 + MediaCodec + Foreground Service
  // iOS: Uses AVFoundation + VideoToolbox + Background modes
  Future<void> startWitnessMode() => channel.invokeMethod('startWitnessMode');
  Future<void> stopWitnessMode() => channel.invokeMethod('stopWitnessMode');
  Future<String> getVideoHash() => channel.invokeMethod('getVideoHash');
}
```

### Backend Stack Details

| Component | Technology | Rationale |
|-----------|------------|-----------|
| Language | Go 1.22+ | Single binary, efficient concurrency, cross-compile |
| Framework | Gin or Echo | Lightweight, fast HTTP router |
| Database | PostgreSQL 16 | JSONB for flexibility, proven reliability |
| Object Storage | S3 API (MinIO self-hosted) | Standard API, self-hostable |
| Queue | Redis Streams or NATS | Lightweight, persistent queues |
| Federation Protocol | Custom (ActivityPub-inspired) | Event-based replication |

### Strengths

| Strength | Explanation |
|----------|-------------|
| **Development velocity** | Single UI codebase, hot reload, strong tooling |
| **Cross-platform consistency** | UI identical on both platforms |
| **Native escape hatch** | Platform channels for anything Flutter can't do |
| **Mature ecosystem** | Large plugin ecosystem, active community |
| **Go efficiency** | Single binary deployment, low resource usage |
| **Self-hostable** | Go binary + PostgreSQL + MinIO = complete node |

### Weaknesses

| Weakness | Mitigation |
|----------|------------|
| **Native code still required** | Budget time for Kotlin/Swift modules (~30% effort) |
| **Flutter camera limitations** | Use platform channels for full Camera2/AVFoundation |
| **Background execution complexity** | Native foreground service required for Android |
| **Learning curve** | Dart less common, but learnable |
| **iOS background limits** | Fundamental iOS limitation, not Flutter-specific |

### Camouflage Implementation

**Android:**
```xml
<!-- AndroidManifest.xml - Activity aliases for icon camouflage -->
<activity-alias
    android:name=".CalculatorAlias"
    android:icon="@mipmap/ic_calculator"
    android:label="Calculator"
    android:targetActivity=".MainActivity"
    android:enabled="false">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity-alias>
```

**iOS:**
- Alternate icons via `UIApplication.setAlternateIconName`
- App name change not possible without App Store update
- Workaround: Submit to App Store as innocuous app (e.g., "Safety Notes")

### Covert Recording (Witness Mode)

**Android Implementation:**
```kotlin
// Native Kotlin module
class WitnessModeService : Service() {
    private lateinit var cameraManager: CameraManager
    private lateinit var mediaRecorder: MediaRecorder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createSilentNotification())
        acquireWakeLock()
        startRecording()
        return START_STICKY
    }

    private fun startRecording() {
        // Camera2 API + MediaCodec for efficient encoding
        // Screen can be off - foreground service keeps process alive
    }
}
```

**iOS Limitations:**
- iOS does not allow true screen-off recording without user interaction
- Best available: Audio recording in background + location tracking
- Video requires app in foreground (but can minimize UI visibility)
- Alternative: Use audio-only witness mode on iOS

### Estimated Development Effort

| Component | Effort | Notes |
|-----------|--------|-------|
| Flutter UI + business logic | 40% | Shared across platforms |
| Android native modules | 25% | Camera, foreground service, camouflage |
| iOS native modules | 20% | AVFoundation, background modes |
| Go backend | 15% | API, federation, storage |
| **Total** | 100% | |

---

## Option 2: Kotlin Multiplatform + Swift UI

### Overview

Kotlin Multiplatform (KMP) shares business logic (encryption, networking, storage) across platforms while using native UI (Jetpack Compose on Android, SwiftUI on iOS). This provides maximum platform integration with significant code sharing.

### Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     KOTLIN MULTIPLATFORM STACK                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  SHARED CODE (Kotlin Multiplatform)                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      Common Module (Kotlin)                        │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │  │
│  │  │  Business   │  │  Network    │  │  Storage    │               │  │
│  │  │   Logic     │  │   Layer     │  │   Layer     │               │  │
│  │  │             │  │   (Ktor)    │  │(SQLDelight) │               │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘               │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │  │
│  │  │  Crypto     │  │  Upload     │  │  Federation │               │  │
│  │  │  Utils      │  │  Manager    │  │  Protocol   │               │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘               │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│         │                                           │                    │
│         │ expect/actual                             │                    │
│         │                                           │                    │
│  ┌──────┴──────────────────┐      ┌────────────────┴─────────────────┐ │
│  │     ANDROID APP         │      │          iOS APP                  │ │
│  │  ┌───────────────────┐  │      │  ┌───────────────────┐           │ │
│  │  │  Jetpack Compose  │  │      │  │      SwiftUI      │           │ │
│  │  │       UI          │  │      │  │        UI         │           │ │
│  │  └───────────────────┘  │      │  └───────────────────┘           │ │
│  │  ┌───────────────────┐  │      │  ┌───────────────────┐           │ │
│  │  │  Platform-specific│  │      │  │  Platform-specific│           │ │
│  │  │  • Camera2        │  │      │  │  • AVFoundation   │           │ │
│  │  │  • ForegroundSvc  │  │      │  │  • Background     │           │ │
│  │  │  • Bluetooth      │  │      │  │  • CoreBluetooth  │           │ │
│  │  └───────────────────┘  │      │  └───────────────────┘           │ │
│  └─────────────────────────┘      └──────────────────────────────────┘ │
│                                                                          │
│  BACKEND: Same Go stack as Option 1                                     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Shared Code (KMP Common Module)

| Layer | Technology | Rationale |
|-------|------------|-----------|
| Networking | Ktor Client | KMP-native, suspending functions |
| Database | SQLDelight | Type-safe SQL, KMP support |
| Serialization | kotlinx.serialization | KMP-native, JSON/Protobuf |
| Crypto | Libsodium (via expect/actual) | Platform bindings |
| Async | Coroutines | KMP-native concurrency |
| DI | Koin | Lightweight, KMP-compatible |

### Platform-Specific Code

**Android (Kotlin):**
| Component | Technology |
|-----------|------------|
| UI | Jetpack Compose |
| Camera | Camera2 / CameraX |
| Background | Foreground Service + WorkManager |
| Bluetooth | Android BLE APIs |
| Encryption | Android Keystore |

**iOS (Swift):**
| Component | Technology |
|-----------|------------|
| UI | SwiftUI |
| Camera | AVFoundation |
| Background | Background Modes + BGTaskScheduler |
| Bluetooth | Core Bluetooth |
| Encryption | CryptoKit + Keychain |

### Strengths

| Strength | Explanation |
|----------|-------------|
| **Full native UI** | Platform-perfect look and feel |
| **Deep platform access** | No abstraction layer for camera/services |
| **Significant code sharing** | 40-60% shared business logic |
| **Type safety across boundary** | Kotlin/Swift interop is clean |
| **Future-proof** | Google and JetBrains actively investing |
| **Native performance** | No bridge overhead |

### Weaknesses

| Weakness | Mitigation |
|----------|------------|
| **Two UI codebases** | Design system documentation, component parity |
| **KMP maturity** | Improving rapidly, 1.9+ is stable |
| **iOS interop complexity** | Kotlin/Native memory model improvements help |
| **Smaller ecosystem** | Growing, but fewer ready-made solutions |
| **Higher initial effort** | Two UIs to build, but faster iteration later |

### Camouflage Implementation

Same as Flutter option - this is fundamentally a platform API issue, not a framework choice.

### Covert Recording

Native implementation identical to Flutter option, but no platform channel bridge needed.

### Estimated Development Effort

| Component | Effort | Notes |
|-----------|--------|-------|
| Shared KMP module | 30% | Business logic, networking, storage |
| Android UI (Compose) | 25% | Full native UI |
| Android platform code | 15% | Camera, services |
| iOS UI (SwiftUI) | 20% | Full native UI |
| iOS platform code | 10% | AVFoundation, background |
| **Total** | 100% | |

---

## Option 3: Native Dual-Platform (Kotlin + Swift)

### Overview

Fully native development with no cross-platform framework. Maximum platform integration and performance, but requires maintaining two completely separate codebases.

### Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        NATIVE DUAL-PLATFORM                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────────────────────┐    ┌──────────────────────────────┐  │
│  │        ANDROID APP           │    │          iOS APP              │  │
│  │         (Kotlin)             │    │          (Swift)              │  │
│  │                              │    │                               │  │
│  │  ┌────────────────────────┐  │    │  ┌────────────────────────┐  │  │
│  │  │    Jetpack Compose     │  │    │  │       SwiftUI          │  │  │
│  │  └────────────────────────┘  │    │  └────────────────────────┘  │  │
│  │  ┌────────────────────────┐  │    │  ┌────────────────────────┐  │  │
│  │  │    Business Logic      │  │    │  │    Business Logic      │  │  │
│  │  │    (ViewModels)        │  │    │  │   (ViewModels/TCA)     │  │  │
│  │  └────────────────────────┘  │    │  └────────────────────────┘  │  │
│  │  ┌────────────────────────┐  │    │  ┌────────────────────────┐  │  │
│  │  │      Data Layer        │  │    │  │      Data Layer        │  │  │
│  │  │  Room + Retrofit +     │  │    │  │  CoreData + URLSession │  │  │
│  │  │      DataStore         │  │    │  │    + UserDefaults      │  │  │
│  │  └────────────────────────┘  │    │  └────────────────────────┘  │  │
│  │  ┌────────────────────────┐  │    │  ┌────────────────────────┐  │  │
│  │  │   Platform Services    │  │    │  │   Platform Services    │  │  │
│  │  │  Camera2, MediaCodec,  │  │    │  │  AVFoundation, Video-  │  │  │
│  │  │  ForegroundService,    │  │    │  │  Toolbox, Background   │  │  │
│  │  │  WorkManager, BLE      │  │    │  │  Modes, CoreBluetooth  │  │  │
│  │  └────────────────────────┘  │    │  └────────────────────────┘  │  │
│  │                              │    │                               │  │
│  └──────────────────────────────┘    └──────────────────────────────┘  │
│                                                                          │
│  SHARED: API specification, federation protocol, design system           │
│                                                                          │
│  BACKEND: Same Go stack as Option 1                                     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Android Stack

| Layer | Technology | Rationale |
|-------|------------|-----------|
| UI | Jetpack Compose | Modern, declarative, Google-recommended |
| Architecture | MVVM + Clean Architecture | Testable, maintainable |
| DI | Hilt | Official Jetpack DI |
| Networking | Retrofit + OkHttp | Industry standard |
| Database | Room | Type-safe, reactive |
| Background | WorkManager + Foreground Service | Guaranteed execution |
| Camera | Camera2 / CameraX | Full control |

### iOS Stack

| Layer | Technology | Rationale |
|-------|------------|-----------|
| UI | SwiftUI | Modern, declarative, Apple-recommended |
| Architecture | TCA or MVVM | Testable, maintainable |
| DI | Manual or Swinject | Swift DI options |
| Networking | URLSession + async/await | Native, efficient |
| Database | CoreData or SwiftData | Native persistence |
| Background | BGTaskScheduler + Background modes | Platform-standard |
| Camera | AVFoundation | Full control |

### Strengths

| Strength | Explanation |
|----------|-------------|
| **Maximum platform integration** | No abstraction between code and platform |
| **Best possible performance** | No bridge, no overhead |
| **Platform-native patterns** | Follow official guidelines exactly |
| **Independent releases** | Can ship platform-specific fixes independently |
| **Hire specialists** | Android devs + iOS devs, not generalists |

### Weaknesses

| Weakness | Mitigation |
|----------|------------|
| **Double the code** | Strong API contracts, shared design system |
| **Feature drift** | Rigorous feature parity testing |
| **Double the bugs** | More testing, but different bugs |
| **Higher cost** | Need two specialized teams |
| **Coordination overhead** | Shared specs, regular sync |

### When to Choose Native

This approach is best when:
- Budget is not a constraint
- Team has deep platform expertise
- Maximum performance is critical
- Platforms have significantly different UX requirements
- Long-term maintenance team is established

### Estimated Development Effort

| Component | Effort | Notes |
|-----------|--------|-------|
| Android app (full) | 45% | Complete app |
| iOS app (full) | 45% | Complete app |
| Backend | 10% | Shared API |
| **Total** | 100% | |

**Note:** Total effort is ~1.5-2x higher than cross-platform options.

---

## Comparison Matrix

### Development Factors

| Factor | Flutter + Go | KMP + SwiftUI | Native Dual |
|--------|--------------|---------------|-------------|
| **Code sharing** | 70-80% | 40-60% | 0-10% |
| **Time to MVP** | Fastest | Medium | Slowest |
| **Team size (MVP)** | 2-3 devs | 3-4 devs | 4-6 devs |
| **Hiring difficulty** | Medium (Dart) | Medium (KMP) | Easy (specialists) |
| **Learning curve** | Medium | Medium-High | Low (if experienced) |

### Technical Factors

| Factor | Flutter + Go | KMP + SwiftUI | Native Dual |
|--------|--------------|---------------|-------------|
| **Native API access** | Via channels | Direct (some limits) | Direct |
| **Camera/video perf** | Good (native modules) | Excellent | Excellent |
| **Background execution** | Good (native required) | Excellent | Excellent |
| **UI flexibility** | Limited to Flutter widgets | Full native | Full native |
| **App size** | ~15-30 MB | ~10-20 MB | ~8-15 MB |
| **Startup time** | Good | Excellent | Excellent |

### Maintenance Factors

| Factor | Flutter + Go | KMP + SwiftUI | Native Dual |
|--------|--------------|---------------|-------------|
| **Single codebase** | Mostly yes | Partially | No |
| **Platform updates** | Wait for plugins | Near-immediate | Immediate |
| **Debugging ease** | Good | Medium | Best |
| **Testing** | Single test suite | Partial sharing | Two suites |

### Risk Factors

| Factor | Flutter + Go | KMP + SwiftUI | Native Dual |
|--------|--------------|---------------|-------------|
| **Framework maturity** | High | Medium-High | Highest |
| **Community size** | Large | Growing | Largest |
| **Corporate backing** | Google | Google + JetBrains | Apple + Google |
| **Longevity risk** | Low | Low | Lowest |

---

## Recommendation

### Primary Recommendation: Flutter + Go

**For the Witness MVP, Flutter + Go provides the best balance of development velocity, cross-platform coverage, and the ability to implement all critical features.**

#### Rationale

1. **MVP Speed**: Single UI codebase means faster iteration and testing
2. **Native Escape Hatch**: Platform channels enable all required native features
3. **Open Source Alignment**: Flutter is fully open source, aligns with Witness philosophy
4. **Go Backend Efficiency**: Single binary deployment, easy for organizations to self-host
5. **Proven for Media Apps**: Apps like eBay Motors, Google Pay use Flutter for camera features

#### Key Implementation Notes

| Feature | Implementation Approach |
|---------|------------------------|
| Witness mode (Android) | Native Kotlin foreground service via platform channel |
| Witness mode (iOS) | Audio background + limited video (iOS constraint) |
| Camouflage | Activity aliases (Android), alternate icons (iOS) |
| Streaming | Native HLS/RTMP via platform channel |
| Encryption | Platform crypto APIs via channel |
| Bluetooth mesh | flutter_blue_plus + native fallback |

#### When to Escalate to KMP

If during development these limitations prove blocking:
- Flutter camera plugins insufficient for quality requirements
- Background execution unreliable
- Platform channel overhead impacts performance

Then consider KMP for v2 or specific modules.

### Alternative Recommendation: KMP + SwiftUI

**Choose this if:**
- Team has strong Kotlin/Swift expertise
- Higher native platform integration is required
- Longer development timeline is acceptable
- UI must perfectly match platform conventions

### Not Recommended for MVP: Native Dual

While this provides the best platform integration, the development overhead is significant for an open-source MVP. Consider for v2 if Witness achieves significant adoption and funding.

---

## Appendix: Technology Notes

### Flutter Relevant Libraries

```yaml
# pubspec.yaml - key dependencies
dependencies:
  # State
  flutter_riverpod: ^2.4.0

  # Storage
  drift: ^2.14.0
  drift_sqflite: ^2.0.0

  # Networking
  dio: ^5.4.0
  web_socket_channel: ^2.4.0

  # Crypto
  pointycastle: ^3.7.0
  cryptography: ^2.5.0

  # Camera (basic)
  camera: ^0.10.5

  # Background
  flutter_background_service: ^5.0.0

  # Bluetooth
  flutter_blue_plus: ^1.28.0

  # Local storage
  shared_preferences: ^2.2.0
  flutter_secure_storage: ^9.0.0
```

### Go Backend Libraries

```go
// go.mod - key dependencies
module witness-node

require (
    github.com/gin-gonic/gin v1.9.1
    github.com/jackc/pgx/v5 v5.5.0
    github.com/minio/minio-go/v7 v7.0.66
    github.com/redis/go-redis/v9 v9.3.0
    github.com/golang-jwt/jwt/v5 v5.2.0
    golang.org/x/crypto v0.17.0
)
```

### KMP Relevant Libraries

```kotlin
// build.gradle.kts - KMP common module
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.ktor:ktor-client-core:2.3.7")
            implementation("app.cash.sqldelight:runtime:2.0.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("io.insert-koin:koin-core:3.5.3")
        }
    }
}
```

### Federation Protocol Sketch

```
WITNESS FEDERATION PROTOCOL (DRAFT)

Message Types:
├── EVIDENCE_CREATED    - New evidence hash announced
├── EVIDENCE_REPLICATE  - Request/push evidence data
├── HASH_VERIFY         - Verify evidence hash exists
├── NODE_ANNOUNCE       - Node joining federation
└── NODE_HEARTBEAT      - Node availability ping

Replication Model:
├── Event-sourced (append-only log)
├── Eventual consistency
├── Minimum 3 node replication before confirming to client
└── Encrypted at rest (node operators cannot view)

Transport:
├── Node-to-node: mTLS + certificate pinning
├── Client-to-node: TLS 1.3 + certificate pinning
└── Protocol: gRPC or REST + WebSocket
```

### Security Considerations

| Layer | Approach |
|-------|----------|
| Transport | TLS 1.3, certificate pinning |
| At-rest (client) | AES-256-GCM, platform keystore |
| At-rest (server) | AES-256-GCM, encrypted at upload |
| E2E encryption | NaCl box (Curve25519 + XSalsa20 + Poly1305) |
| Key storage | Android Keystore, iOS Keychain |
| Anonymous auth | Device-generated keys, no PII |

---

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-25 | Initial research complete |

---

*This document is a living specification and will be updated as the project evolves.*
